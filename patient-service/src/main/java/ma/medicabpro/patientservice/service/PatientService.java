package ma.medicabpro.patientservice.service;

import ma.medicabpro.patientservice.dto.PatientRequestDTO;
import ma.medicabpro.patientservice.dto.PatientResponseDTO;

import java.util.List;

public interface PatientService {

    // ── CRUD ───────────────────────────────
    PatientResponseDTO creerPatient(
            PatientRequestDTO dto);

    PatientResponseDTO getPatientById(Long id);

    PatientResponseDTO getPatientByCin(String cin);

    List<PatientResponseDTO> getAllPatients(
            Long tenantId);

    List<PatientResponseDTO> rechercherPatients(
            Long tenantId, String query);

    PatientResponseDTO modifierPatient(
            Long id, PatientRequestDTO dto);

    void desactiverPatient(Long id);

    // ── Statistiques ───────────────────────
    long countPatients(Long tenantId);
}
