from pydantic import BaseModel, Field
from typing import Optional, List, Dict, Any
from datetime import datetime


class PatientContext(BaseModel):
    """Données patient injectées dynamiquement dans le prompt MedAgent."""
    nom_medecin: str = "Dr. Unknown"
    specialite: str = "Médecine générale"
    nom_patient: str = "Patient inconnu"
    age: str = "N/A"
    sexe: str = "N/A"
    antecedents: str = "Aucun"
    allergies: str = "Aucune connue"
    medicaments_actuels: str = "Aucun"
    derniere_visite: str = "N/A"


class ChatMessage(BaseModel):
    """Message entrant pour l'endpoint REST /api/ai/chat."""
    session_id: str
    message: str
    source: str = "chat"  # "chat" | "whisper" | "commande"
    context: Optional[PatientContext] = None


class CommandRequest(BaseModel):
    """Requête pour les commandes système (GENERER_CR, GENERER_ORDONNANCE, RESET)."""
    commande: str  # "GENERER_CR" | "GENERER_ORDONNANCE" | "RESET"
    session_id: str
    context: Optional[PatientContext] = None


class AgentResponse(BaseModel):
    """Réponse retournée par l'agent MedAgent."""
    session_id: str
    response: str
    mode: str  # "DICTEE" | "CHAT" | "COMMANDE"
    alerts: List[Dict[str, Any]] = Field(default_factory=list)
    session_data_summary: Optional[Dict[str, Any]] = None


class TranscriptionResponse(BaseModel):
    """Réponse après transcription audio Whisper + traitement agent."""
    session_id: str
    transcription: str
    agent_response: str
    alerts: List[Dict[str, Any]] = Field(default_factory=list)


class DrugAlert(BaseModel):
    """Alerte médicament (allergie ou interaction)."""
    level: str  # "ROUGE" | "ORANGE"
    type: str   # "ALLERGIE" | "INTERACTION" | "POSOLOGIE"
    description: str
    action_required: str
    alternative: Optional[str] = None


# ─── Session Data ────────────────────────────────────────────────────────────

class SessionData(BaseModel):
    """Données cliniques accumulées pendant la consultation."""
    session_id: str
    created_at: datetime = Field(default_factory=datetime.utcnow)
    patient_context: Optional[PatientContext] = None

    # Données cliniques extraites
    motif: Optional[str] = None
    symptomes: List[str] = Field(default_factory=list)
    duree: Optional[str] = None
    parametres_vitaux: Dict[str, str] = Field(default_factory=dict)
    examen_clinique: Optional[str] = None
    diagnostic: Optional[str] = None
    medicaments: List[Dict[str, str]] = Field(default_factory=list)
    examens_complementaires: List[str] = Field(default_factory=list)
    suivi: Optional[str] = None
    alertes: List[DrugAlert] = Field(default_factory=list)

    # Historique conversation (pour le LLM)
    conversation_history: List[Dict[str, str]] = Field(default_factory=list)
