"""
LLM Service — Groq Llama 3.3 70B (primary) + Gemini 2.0 Flash (fallback)
"""

import os
import asyncio
import logging
import warnings
from typing import AsyncGenerator

import httpx
from groq import AsyncGroq
from dotenv import load_dotenv

load_dotenv()
logger = logging.getLogger(__name__)

GROQ_API_KEY    = os.getenv("GROQ_API_KEY", "")
GEMINI_API_KEY  = os.getenv("GEMINI_API_KEY", "")
GROQ_LLM_MODEL  = os.getenv("GROQ_LLM_MODEL", "llama-3.3-70b-versatile")
GEMINI_MODEL    = os.getenv("GEMINI_MODEL", "gemini-2.0-flash")
GROQ_SSL_VERIFY = os.getenv("GROQ_SSL_VERIFY", "true").lower() != "false"

if not GROQ_SSL_VERIFY:
    warnings.filterwarnings("ignore", message="Unverified HTTPS request")
    import urllib3
    urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)
    logger.warning("⚠️  Groq SSL verification DISABLED (proxy detected) — ne pas utiliser en production")

_groq_client: AsyncGroq | None = None


def get_groq_client() -> AsyncGroq:
    global _groq_client
    if _groq_client is None:
        if not GROQ_SSL_VERIFY:
            # Proxy SSL — passer un httpx client sans vérification SSL
            http_client = httpx.AsyncClient(verify=False, timeout=30.0)
            _groq_client = AsyncGroq(api_key=GROQ_API_KEY, http_client=http_client)
        else:
            _groq_client = AsyncGroq(api_key=GROQ_API_KEY, timeout=30.0)
    return _groq_client



async def _call_gemini(messages: list[dict], system_prompt: str) -> str:
    """
    Call Gemini using the new google.genai SDK (replaces deprecated google.generativeai).
    """
    try:
        from google import genai
        from google.genai import types

        client = genai.Client(api_key=GEMINI_API_KEY)

        # Build conversation contents
        contents = []
        for msg in messages:
            role = "user" if msg["role"] == "user" else "model"
            contents.append(types.Content(role=role, parts=[types.Part(text=msg["content"])]))

        config = types.GenerateContentConfig(
            system_instruction=system_prompt,
            max_output_tokens=2048,
            temperature=0.3,
        )

        response = await client.aio.models.generate_content(
            model=GEMINI_MODEL,
            contents=contents,
            config=config,
        )
        return response.text or ""

    except Exception as e:
        raise RuntimeError(f"Gemini failed: {e}") from e


async def call_llm(
    messages: list[dict],
    system_prompt: str,
    stream: bool = False,
) -> str:
    """
    Call Groq Llama 3.3 70B. Falls back to Gemini 2.0 Flash on failure.

    Args:
        messages: Conversation history [{"role": "user"|"assistant", "content": "..."}]
        system_prompt: The MedAgent system prompt (with injected session vars)
        stream: Unused (kept for signature compatibility)

    Returns:
        Full response string
    """
    full_messages = [{"role": "system", "content": system_prompt}] + messages

    # ── Try Groq first ──────────────────────────────────────────────────────
    try:
        client = get_groq_client()
        response = await client.chat.completions.create(
            model=GROQ_LLM_MODEL,
            messages=full_messages,
            max_tokens=2048,
            temperature=0.3,
        )
        return response.choices[0].message.content or ""
    except Exception as groq_err:
        logger.warning(f"Groq LLM failed: {groq_err}. Trying Gemini fallback...")

    # ── Fallback: Gemini ─────────────────────────────────────────────────────
    try:
        return await _call_gemini(messages, system_prompt)
    except Exception as gemini_err:
        logger.error(f"Gemini fallback also failed: {gemini_err}")
        raise RuntimeError(
            "Both Groq and Gemini LLMs are unavailable. Check your API keys."
        ) from gemini_err


async def stream_llm(
    messages: list[dict],
    system_prompt: str,
) -> AsyncGenerator[str, None]:
    """
    Stream tokens from Groq Llama 3.3 70B for WebSocket real-time display.
    Falls back to non-streaming Gemini on error.

    Yields:
        Token chunks (strings) as they arrive.
    """
    full_messages = [{"role": "system", "content": system_prompt}] + messages

    try:
        # ✅ Correct Groq async streaming: create(stream=True)
        client = get_groq_client()
        stream = await client.chat.completions.create(
            model=GROQ_LLM_MODEL,
            messages=full_messages,
            max_tokens=2048,
            temperature=0.3,
            stream=True,
        )
        async for chunk in stream:
            delta = chunk.choices[0].delta.content
            if delta:
                yield delta
        return  # ✅ success — don't fall through to Gemini

    except Exception as e:
        logger.warning(f"Groq streaming failed: {e}. Falling back to Gemini (non-streaming).")

    # Fallback: Gemini — yield full response as one chunk
    try:
        result = await _call_gemini(messages, system_prompt)
        yield result
    except Exception as fallback_err:
        logger.error(f"All LLMs failed: {fallback_err}")
        yield f"❌ Erreur interne IA: Les deux services IA (Groq et Gemini) sont temporairement indisponibles. Veuillez réessayer dans quelques minutes."
