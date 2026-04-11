"""
MedAgent Core — Dual-mode medical AI agent.
Manages SESSION_DATA per session and routes messages to the correct mode handler.
"""

import logging
from datetime import datetime
from typing import Dict, Any

from agent.prompt import build_system_prompt, build_user_message
from agent.drug_checker import check_drug_safety
from services.llm import call_llm, stream_llm
from models.session import SessionData, PatientContext

logger = logging.getLogger(__name__)

# In-memory session store: session_id → SessionData
# In production, replace with Redis for multi-instance support
_sessions: Dict[str, SessionData] = {}


def get_or_create_session(session_id: str, context: PatientContext | None = None) -> SessionData:
    """Get existing session or create a new one."""
    if session_id not in _sessions:
        _sessions[session_id] = SessionData(
            session_id=session_id,
            patient_context=context,
        )
    elif context:
        # Update context if provided on subsequent calls
        _sessions[session_id].patient_context = context
    return _sessions[session_id]


def reset_session(session_id: str) -> str:
    """Clear session data for a given session_id."""
    if session_id in _sessions:
        _sessions[session_id] = SessionData(
            session_id=session_id,
            patient_context=_sessions[session_id].patient_context,
        )
    return "Session réinitialisée. Nouvelle consultation prête."


def _context_to_dict(context: PatientContext | None) -> dict:
    """Convert PatientContext model to dict for prompt injection."""
    if context is None:
        return {}
    return context.model_dump()


async def _run_allergy_check(session: SessionData, response_text: str) -> list[dict]:
    """
    Scan LLM response for mentioned drug names and run safety checks.
    Basic heuristic: look for common French drug mention patterns.
    """
    if not session.patient_context:
        return []

    alerts = []
    # Extract drugs from session.medicaments list (updated by LLM)
    drugs_to_check = [m.get("nom", "") for m in session.medicaments if m.get("nom")]

    for drug in drugs_to_check:
        drug_alerts = await check_drug_safety(
            drug_name=drug,
            patient_allergies=session.patient_context.allergies,
            current_medications=session.patient_context.medicaments_actuels,
        )
        alerts.extend(drug_alerts)

    return alerts


async def process_message(
    session_id: str,
    message: str,
    source: str,  # "whisper" | "chat" | "commande"
    context: PatientContext | None = None,
) -> dict:
    """
    Main entry point for MedAgent processing.

    Args:
        session_id: Unique consultation session ID
        message: Raw user text (or transcription from Whisper)
        source: Message origin ("whisper", "chat", "commande")
        context: Patient/doctor context (used on first call or to update)

    Returns:
        dict with keys: response, mode, alerts, session_id
    """
    session = get_or_create_session(session_id, context)
    context_dict = _context_to_dict(session.patient_context)
    system_prompt = build_system_prompt(context_dict)

    # Build prefixed user message
    user_message = build_user_message(source, message)

    # Detect mode from prefix
    if source == "whisper":
        mode = "DICTEE"
    elif source == "commande":
        mode = "COMMANDE"
    else:
        mode = "CHAT"

    # Add to conversation history
    session.conversation_history.append({
        "role": "user",
        "content": user_message,
    })

    # Call LLM
    try:
        response = await call_llm(
            messages=session.conversation_history,
            system_prompt=system_prompt,
            stream=False,
        )
    except RuntimeError as e:
        logger.error(f"LLM call failed for session {session_id}: {e}")
        return {
            "session_id": session_id,
            "response": "❌ Service IA temporairement indisponible. Vérifiez vos clés API.",
            "mode": mode,
            "alerts": [],
        }

    # Add assistant response to history
    session.conversation_history.append({
        "role": "assistant",
        "content": response,
    })

    # Run drug safety checks
    alerts = await _run_allergy_check(session, response)
    session.alertes.extend(alerts)  # type: ignore

    return {
        "session_id": session_id,
        "response": response,
        "mode": mode,
        "alerts": [a if isinstance(a, dict) else a.model_dump() for a in alerts],
    }


async def stream_message(
    session_id: str,
    message: str,
    source: str,
    context: PatientContext | None = None,
):
    """
    Streaming version of process_message for WebSocket use.
    Yields token chunks as they arrive from the LLM.
    """
    session = get_or_create_session(session_id, context)
    context_dict = _context_to_dict(session.patient_context)
    system_prompt = build_system_prompt(context_dict)

    user_message = build_user_message(source, message)
    session.conversation_history.append({"role": "user", "content": user_message})

    full_response = ""
    async for chunk in stream_llm(
        messages=session.conversation_history,
        system_prompt=system_prompt,
    ):
        full_response += chunk
        yield chunk

    session.conversation_history.append({"role": "assistant", "content": full_response})
