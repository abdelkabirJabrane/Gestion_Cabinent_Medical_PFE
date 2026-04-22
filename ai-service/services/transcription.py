"""
Transcription service using Groq Whisper Large v3.
Converts audio files to text in ~3 seconds per 10 minutes of audio.
"""

import os
import logging
import warnings
from pathlib import Path

import httpx
from groq import AsyncGroq
from dotenv import load_dotenv

load_dotenv()
logger = logging.getLogger(__name__)

GROQ_API_KEY        = os.getenv("GROQ_API_KEY", "")
GROQ_WHISPER_MODEL  = os.getenv("GROQ_WHISPER_MODEL", "whisper-large-v3")
GROQ_SSL_VERIFY     = os.getenv("GROQ_SSL_VERIFY", "true").lower() != "false"

# ── Désactiver les avertissements SSL si proxy détecté ──────────────────────
if not GROQ_SSL_VERIFY:
    warnings.filterwarnings("ignore", message="Unverified HTTPS request")
    try:
        import urllib3
        urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)
    except ImportError:
        pass
    logger.warning("⚠️  Transcription: SSL verification DISABLED (proxy detected)")

_groq_client: AsyncGroq | None = None


def get_groq_client() -> AsyncGroq:
    """
    Returns a shared AsyncGroq client.
    When GROQ_SSL_VERIFY=false, uses an httpx client without SSL verification
    to bypass corporate proxy SSL inspection.
    """
    global _groq_client
    if _groq_client is None:
        if not GROQ_SSL_VERIFY:
            # Même configuration que llm.py : contournement proxy SSL
            http_client = httpx.AsyncClient(verify=False, timeout=60.0)
            _groq_client = AsyncGroq(api_key=GROQ_API_KEY, http_client=http_client)
        else:
            _groq_client = AsyncGroq(api_key=GROQ_API_KEY, timeout=60.0)
    return _groq_client


async def transcribe_audio(audio_bytes: bytes, filename: str = "audio.webm") -> str:
    """
    Transcribe audio bytes using Groq Whisper Large v3.

    Args:
        audio_bytes: Raw audio file bytes (webm, mp3, wav, m4a, ogg, flac)
        filename: Original filename with extension (used to detect format)

    Returns:
        Transcribed text string
    """
    client = get_groq_client()

    try:
        transcription = await client.audio.transcriptions.create(
            file=(filename, audio_bytes),
            model=GROQ_WHISPER_MODEL,
            language="fr",          # French primary; Whisper auto-detects Arabic too
            response_format="text",
        )
        return transcription.strip() if isinstance(transcription, str) else transcription.text.strip()
    except Exception as e:
        logger.error(f"Whisper transcription failed: {e}")
        raise RuntimeError(f"Transcription failed: {e}") from e
