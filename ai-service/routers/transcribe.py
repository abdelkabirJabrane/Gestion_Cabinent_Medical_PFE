"""
Transcription router — Audio upload → Whisper → MedAgent response.
Endpoint: POST /api/ai/transcribe
"""

import json
import logging

from fastapi import APIRouter, UploadFile, File, Form, HTTPException
from models.session import PatientContext, TranscriptionResponse
from services.transcription import transcribe_audio
from agent.medagent import process_message

logger = logging.getLogger(__name__)
router = APIRouter()


@router.post("/api/ai/transcribe", response_model=TranscriptionResponse)
async def transcribe_and_process(
    audio: UploadFile = File(..., description="Audio file (webm, mp3, wav, m4a, ogg)"),
    session_id: str = Form(..., description="Session ID for the consultation"),
    context: str = Form(
        default="{}",
        description="JSON string of PatientContext fields",
    ),
):
    """
    Upload an audio recording → Transcribe with Groq Whisper → Process with MedAgent.

    Flow:
    1. Receive audio file
    2. Transcribe via Groq Whisper Large v3 (~3 sec / 10 min audio)
    3. Send transcription to MedAgent as [DICTEE] message
    4. Return transcription + agent response (+ any drug alerts)
    """
    # Parse context JSON
    patient_context = None
    try:
        context_data = json.loads(context)
        if context_data:
            patient_context = PatientContext(**context_data)
    except Exception as e:
        logger.warning(f"Could not parse context JSON: {e}")

    # Read audio bytes
    audio_bytes = await audio.read()
    if not audio_bytes:
        raise HTTPException(status_code=400, detail="Empty audio file")

    # Transcribe
    try:
        transcription = await transcribe_audio(audio_bytes, filename=audio.filename or "audio.webm")
    except RuntimeError as e:
        raise HTTPException(status_code=503, detail=str(e))

    if not transcription:
        raise HTTPException(status_code=422, detail="Transcription returned empty text")

    # Process with MedAgent as dictation
    result = await process_message(
        session_id=session_id,
        message=transcription,
        source="whisper",
        context=patient_context,
    )

    return TranscriptionResponse(
        session_id=session_id,
        transcription=transcription,
        agent_response=result["response"],
        alerts=result.get("alerts", []),
    )
