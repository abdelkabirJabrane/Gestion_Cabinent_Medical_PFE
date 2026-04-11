"""
LLM Service — Groq Llama 3.3 70B (primary) + Gemini 2.0 Flash (fallback)
"""

import os
import logging
from typing import AsyncGenerator

from groq import AsyncGroq
from dotenv import load_dotenv

load_dotenv()
logger = logging.getLogger(__name__)

GROQ_API_KEY = os.getenv("GROQ_API_KEY", "")
GEMINI_API_KEY = os.getenv("GEMINI_API_KEY", "")
GROQ_LLM_MODEL = os.getenv("GROQ_LLM_MODEL", "llama-3.3-70b-versatile")
GEMINI_MODEL = os.getenv("GEMINI_MODEL", "gemini-2.0-flash")

_groq_client: AsyncGroq | None = None


def get_groq_client() -> AsyncGroq:
    global _groq_client
    if _groq_client is None:
        _groq_client = AsyncGroq(api_key=GROQ_API_KEY)
    return _groq_client


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
        stream: If True, use streaming (for WebSocket use stream_llm instead)

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

    # ── Fallback: Gemini 2.0 Flash ───────────────────────────────────────────
    try:
        import google.generativeai as genai
        genai.configure(api_key=GEMINI_API_KEY)
        model = genai.GenerativeModel(
            model_name=GEMINI_MODEL,
            system_instruction=system_prompt,
        )
        # Convert OpenAI-style messages to Gemini format
        gemini_history = []
        for msg in messages[:-1]:  # all but last
            gemini_history.append({
                "role": "user" if msg["role"] == "user" else "model",
                "parts": [msg["content"]],
            })

        chat = model.start_chat(history=gemini_history)
        last_msg = messages[-1]["content"] if messages else ""
        response = await chat.send_message_async(last_msg)
        return response.text
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
        client = get_groq_client()
        async with client.chat.completions.stream(
            model=GROQ_LLM_MODEL,
            messages=full_messages,
            max_tokens=2048,
            temperature=0.3,
        ) as stream:
            async for chunk in stream:
                delta = chunk.choices[0].delta.content
                if delta:
                    yield delta
    except Exception as e:
        logger.warning(f"Groq streaming failed: {e}. Falling back to Gemini (non-streaming).")
        # Fallback: yield full Gemini response as one chunk
        result = await call_llm(messages, system_prompt, stream=False)
        yield result
