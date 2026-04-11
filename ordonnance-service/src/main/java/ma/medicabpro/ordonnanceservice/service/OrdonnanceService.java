package ma.medicabpro.ordonnanceservice.service;

import ma.medicabpro.ordonnanceservice.dto.OrdonnanceRequestDTO;
import ma.medicabpro.ordonnanceservice.dto.OrdonnanceResponseDTO;

import java.util.List;

public interface OrdonnanceService {

    OrdonnanceResponseDTO creerOrdonnance(OrdonnanceRequestDTO dto);

    OrdonnanceResponseDTO getOrdonnanceById(Long id);

    OrdonnanceResponseDTO getByConsultationId(Long consultationId);

    List<OrdonnanceResponseDTO> getByPatient(Long patientId);

    List<OrdonnanceResponseDTO> getByMedecin(Long medecinId);

    List<OrdonnanceResponseDTO> getByTenant(Long tenantId);

    List<OrdonnanceResponseDTO> getByTenantAndStatut(Long tenantId, String statut);

    OrdonnanceResponseDTO modifierOrdonnance(Long id, OrdonnanceRequestDTO dto);

    OrdonnanceResponseDTO changerStatut(Long id, String statut);

    void supprimerOrdonnance(Long id);

    long countOrdonnances(Long tenantId);
}
