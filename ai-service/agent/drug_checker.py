"""
Drug interaction & allergy checker using free public APIs:
- OpenFDA API: https://open.fda.gov/apis/drug/
- RxNorm API: https://rxnav.nlm.nih.gov/
"""

import httpx
import logging

logger = logging.getLogger(__name__)

OPENFDA_BASE = "https://api.fda.gov/drug/label.json"
RXNORM_BASE = "https://rxnav.nlm.nih.gov/REST"


async def check_drug_allergies(drug_name: str, patient_allergies: str) -> dict | None:
    """
    Check if a prescribed drug matches any known patient allergy.
    Uses simple substring matching + OpenFDA to resolve brand/generic names.
    Returns an alert dict if an allergy is detected, None otherwise.
    """
    if not patient_allergies or patient_allergies.lower() in ("aucune", "aucune connue", "n/a", ""):
        return None

    allergies_list = [a.strip().lower() for a in patient_allergies.split(",")]
    drug_lower = drug_name.lower()

    # Direct match
    for allergen in allergies_list:
        if allergen and (allergen in drug_lower or drug_lower in allergen):
            return {
                "level": "ROUGE",
                "type": "ALLERGIE",
                "description": f"⚠️ Allergie connue : {drug_name} est contre-indiqué (allergie à {allergen})",
                "action_required": "NE PAS prescrire. Choisir une alternative.",
                "alternative": None,
            }

    # Try to resolve via RxNorm for related ingredient check
    try:
        async with httpx.AsyncClient(timeout=5.0) as client:
            resp = await client.get(
                f"{RXNORM_BASE}/drugs.json",
                params={"name": drug_name}
            )
            if resp.status_code == 200:
                data = resp.json()
                concept_group = data.get("drugGroup", {}).get("conceptGroup", [])
                for group in concept_group:
                    for concept in group.get("conceptProperties", []):
                        ingredient = concept.get("name", "").lower()
                        for allergen in allergies_list:
                            if allergen and (allergen in ingredient or ingredient in allergen):
                                return {
                                    "level": "ROUGE",
                                    "type": "ALLERGIE",
                                    "description": f"⚠️ Composant actif ({ingredient}) de {drug_name} détecté : allergie connue à {allergen}",
                                    "action_required": "NE PAS prescrire. Choisir une alternative.",
                                    "alternative": None,
                                }
    except Exception as e:
        logger.warning(f"RxNorm check failed for {drug_name}: {e}")

    return None


async def check_drug_interactions(drug_name: str, current_medications: str) -> dict | None:
    """
    Check for potential interactions between a new drug and current medications.
    Uses OpenFDA drug label API to find interaction warnings.
    Returns an alert dict if an interaction is found, None otherwise.
    """
    if not current_medications or current_medications.lower() in ("aucun", "n/a", ""):
        return None

    current_list = [m.strip().lower() for m in current_medications.split(",")]

    try:
        async with httpx.AsyncClient(timeout=8.0) as client:
            resp = await client.get(
                OPENFDA_BASE,
                params={
                    "search": f'openfda.generic_name:"{drug_name}"',
                    "limit": 1,
                }
            )
            if resp.status_code == 200:
                results = resp.json().get("results", [])
                if results:
                    interactions_text = " ".join(
                        results[0].get("drug_interactions", [])
                    ).lower()
                    for current_med in current_list:
                        if current_med and current_med in interactions_text:
                            return {
                                "level": "ORANGE",
                                "type": "INTERACTION",
                                "description": f"🟠 Interaction possible entre {drug_name} et {current_med} (médicament actuel du patient)",
                                "action_required": "Vérifier la posologie ou choisir une alternative.",
                                "alternative": None,
                            }
    except Exception as e:
        logger.warning(f"OpenFDA interaction check failed for {drug_name}: {e}")

    return None


async def check_drug_safety(
    drug_name: str,
    patient_allergies: str,
    current_medications: str,
) -> list[dict]:
    """
    Full drug safety check: allergy + interaction.
    Returns a list of alerts (empty = safe).
    """
    alerts = []

    allergy_alert = await check_drug_allergies(drug_name, patient_allergies)
    if allergy_alert:
        alerts.append(allergy_alert)

    if not allergy_alert:  # Only check interactions if no allergy blocker
        interaction_alert = await check_drug_interactions(drug_name, current_medications)
        if interaction_alert:
            alerts.append(interaction_alert)

    return alerts
