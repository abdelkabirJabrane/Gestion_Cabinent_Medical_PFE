package ma.medicabpro.billingservice.service;


import ma.medicabpro.billingservice.dto.FactureRequestDTO;
import ma.medicabpro.billingservice.dto.FactureResponseDTO;
import ma.medicabpro.billingservice.dto.PaiementRequestDTO;

import java.util.List;

public interface FactureService {

    FactureResponseDTO creerFacture(
            FactureRequestDTO dto);

    FactureResponseDTO getFactureById(Long id);

    List<FactureResponseDTO> getByPatient(
            Long patientId);

    List<FactureResponseDTO> getByTenant(
            Long tenantId);

    List<FactureResponseDTO> getByStatut(
            Long tenantId, String statut);

    FactureResponseDTO payerFacture(
            PaiementRequestDTO dto);

    FactureResponseDTO annulerFacture(Long id);

    long countFactures(Long tenantId);

    Double getTotalEncaisse(Long tenantId);

    Double getTotalImpaye(Long tenantId);
}