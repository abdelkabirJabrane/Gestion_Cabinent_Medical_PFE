package ma.medicabpro.medicalrecordservice.service;

// ConsultationService.java


import ma.medicabpro.medicalrecordservice.dto.ConsultationRequestDTO;
import ma.medicabpro.medicalrecordservice.dto.ConsultationResponseDTO;

import java.util.List;

public interface ConsultationService {
    ConsultationResponseDTO creerConsultation(ConsultationRequestDTO dto);
    ConsultationResponseDTO getConsultationById(Long id);
    List<ConsultationResponseDTO> getByDossier(Long dossierId);
    List<ConsultationResponseDTO> getByMedecin(Long medecinId);
    List<ConsultationResponseDTO> getByTenant(Long tenantId);
    ConsultationResponseDTO modifierConsultation(Long id, ConsultationRequestDTO dto);
    long countConsultations(Long tenantId);
}