from unittest.mock import patch

def test_reset_command(client):
    """Test the RESET command which clears the session."""
    payload = {
        "session_id": "test-session",
        "commande": "RESET",
        "context": {}
    }
    response = client.post("/api/ai/command", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert data["command"] == "RESET"
    assert data["session_id"] == "test-session"

def test_invalid_command(client):
    """Test an unknown command returns 400."""
    payload = {
        "session_id": "test-session",
        "commande": "INVALID",
        "context": {}
    }
    response = client.post("/api/ai/command", json=payload)
    assert response.status_code == 400
    assert "Unknown command" in response.json()["detail"]

@patch("agent.medagent.call_llm")
def test_rest_chat(mock_call_llm, client):
    """Test the REST chat endpoint with mocked LLM."""
    mock_call_llm.return_value = "Bonjour, je suis votre assistant."
    
    payload = {
        "session_id": "chat-session",
        "message": "Bonjour",
        "source": "chat",
        "context": None
    }
    response = client.post("/api/ai/chat", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert data["response"] == "Bonjour, je suis votre assistant."
    assert data["mode"] == "CHAT"
