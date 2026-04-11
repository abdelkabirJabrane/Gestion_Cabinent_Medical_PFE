"""
WebSocket router — Real-time streaming chat for MedAgent.
Endpoint: WS /ws/chat/{session_id}

Message format from client (JSON):
{
  "message": "le patient a de la fièvre",
  "source": "chat",   // "chat" | "whisper" | "commande"
  "context": { ... }  // PatientContext fields (optional after first message)
}
"""

import json
import logging
from fastapi import APIRouter, WebSocket, WebSocketDisconnect, HTTPException
from models.session import PatientContext, ChatMessage, AgentResponse
from agent.medagent import process_message, stream_message

logger = logging.getLogger(__name__)
router = APIRouter()


@router.websocket("/ws/chat/{session_id}")
async def websocket_chat(websocket: WebSocket, session_id: str):
    """
    WebSocket endpoint for real-time streaming MedAgent chat.
    Tokens stream word-by-word (ChatGPT-style).
    """
    await websocket.accept()
    logger.info(f"WebSocket connected: session={session_id}")

    try:
        while True:
            raw = await websocket.receive_text()
            try:
                data = json.loads(raw)
            except json.JSONDecodeError:
                await websocket.send_text(json.dumps({"error": "Invalid JSON"}))
                continue

            message = data.get("message", "")
            source = data.get("source", "chat")
            context_data = data.get("context")

            context = None
            if context_data:
                try:
                    context = PatientContext(**context_data)
                except Exception:
                    pass

            if not message:
                await websocket.send_text(json.dumps({"error": "Empty message"}))
                continue

            # Stream tokens in real-time
            await websocket.send_text(json.dumps({"type": "start", "session_id": session_id}))

            async for chunk in stream_message(session_id, message, source, context):
                await websocket.send_text(json.dumps({"type": "chunk", "content": chunk}))

            await websocket.send_text(json.dumps({"type": "end", "session_id": session_id}))

    except WebSocketDisconnect:
        logger.info(f"WebSocket disconnected: session={session_id}")
    except Exception as e:
        logger.error(f"WebSocket error for session {session_id}: {e}")
        try:
            await websocket.send_text(json.dumps({"error": str(e)}))
        except Exception:
            pass


@router.post("/api/ai/chat", response_model=AgentResponse)
async def rest_chat(payload: ChatMessage):
    """
    REST alternative to WebSocket — returns full response at once.
    Use WebSocket for real-time streaming; use this for simple integrations.
    """
    result = await process_message(
        session_id=payload.session_id,
        message=payload.message,
        source=payload.source,
        context=payload.context,
    )
    return AgentResponse(**result)
