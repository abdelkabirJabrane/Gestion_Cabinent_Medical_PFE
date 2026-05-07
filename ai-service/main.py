"""
MedAgent AI Service — FastAPI Application Entry Point
Port: 8000
"""

import os
import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from dotenv import load_dotenv

from routers import chat, transcribe, commands

load_dotenv()

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s | %(levelname)s | %(name)s | %(message)s",
)
logger = logging.getLogger(__name__)

APP_PORT = int(os.getenv("APP_PORT", 8000))


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Startup / shutdown hooks."""
    logger.info("🚀 MedAgent AI Service starting up...")
    logger.info(f"   Groq API Key: {'✓ Set' if os.getenv('GROQ_API_KEY') else '✗ MISSING'}")
    logger.info(f"   Gemini API Key: {'✓ Set' if os.getenv('GEMINI_API_KEY') else '✗ Not set (optional)'}")
    yield
    logger.info("👋 MedAgent AI Service shutting down...")


app = FastAPI(
    title="MedAgent AI Service",
    description=(
        "Dual-mode medical AI agent: conversational assistant + medical dictation transcription. "
        "Powered by Groq Whisper (transcription) and Llama 3.3 70B (LLM)."
    ),
    version="1.0.0",
    lifespan=lifespan,
    docs_url="/docs",
    redoc_url="/redoc",
)

# ── CORS ─────────────────────────────────────────────────────────────────────
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],   # Restrict in production to your frontend URL
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ── Routers ───────────────────────────────────────────────────────────────────
app.include_router(chat.router, tags=["Chat & WebSocket"])
app.include_router(transcribe.router, tags=["Transcription"])
app.include_router(commands.router, tags=["Commands"])


# ── Health Check ─────────────────────────────────────────────────────────────
@app.get("/health", tags=["Health"])
async def health_check():
    return {
        "status": "ok",
        "service": "MedAgent v1",
        "groq_configured": bool(os.getenv("GROQ_API_KEY")),
        "gemini_configured": bool(os.getenv("GEMINI_API_KEY")),
    }


# ── Dev entrypoint ───────────────────────────────────────────────────────────
if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "main:app",
        host=os.getenv("AI_SERVICE_HOST", "0.0.0.0"),
        port=APP_PORT,
        reload=True,
        log_level="info",
    )
