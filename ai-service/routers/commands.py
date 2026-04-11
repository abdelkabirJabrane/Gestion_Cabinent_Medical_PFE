"""
Commands router — System commands: GENERER_CR, GENERER_ORDONNANCE, RESET.
Endpoint: POST /api/ai/command
"""

import logging

from fastapi import APIRouter, HTTPException
from models.session import CommandRequest
from agent.medagent import process_message, reset_session, get_or_create_session

logger = logging.getLogger(__name__)
router = APIRouter()

VALID_COMMANDS = {"GENERER_CR", "GENERER_ORDONNANCE", "RESET"}


@router.post("/api/ai/command")
async def execute_command(payload: CommandRequest):
    """
    Execute a system command on the current session.

    Commands:
    - GENERER_CR          → Generate full consultation report (JSON)
    - GENERER_ORDONNANCE  → Generate prescription (JSON)
    - RESET               → Clear session data
    """
    cmd = payload.commande.strip().upper()

    if cmd not in VALID_COMMANDS:
        raise HTTPException(
            status_code=400,
            detail=f"Unknown command '{cmd}'. Valid: {', '.join(VALID_COMMANDS)}",
        )

    if cmd == "RESET":
        msg = reset_session(payload.session_id)
        return {"session_id": payload.session_id, "response": msg, "command": "RESET"}

    # GENERER_CR and GENERER_ORDONNANCE → send as [COMMANDE] to MedAgent
    result = await process_message(
        session_id=payload.session_id,
        message=cmd,
        source="commande",
        context=payload.context,
    )

    # Try to parse JSON from LLM response
    import json, re
    response_text = result["response"]

    # Extract JSON block if wrapped in markdown code fences
    json_match = re.search(r"```(?:json)?\s*(\{.*?\})\s*```", response_text, re.DOTALL)
    if json_match:
        try:
            parsed = json.loads(json_match.group(1))
            return {
                "session_id": payload.session_id,
                "command": cmd,
                "data": parsed,
                "alerts": result.get("alerts", []),
            }
        except json.JSONDecodeError:
            pass

    # Try raw JSON parse
    try:
        parsed = json.loads(response_text)
        return {
            "session_id": payload.session_id,
            "command": cmd,
            "data": parsed,
            "alerts": result.get("alerts", []),
        }
    except json.JSONDecodeError:
        # Return raw text if LLM didn't produce valid JSON
        return {
            "session_id": payload.session_id,
            "command": cmd,
            "data": None,
            "raw_response": response_text,
            "alerts": result.get("alerts", []),
        }
