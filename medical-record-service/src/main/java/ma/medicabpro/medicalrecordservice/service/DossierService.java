package ma.medicabpro.medicalrecordservice.service;


import ma.medicabpro.medicalrecordservice.dto.DossierRequestDTO;
import ma.medicabpro.medicalrecordservice.dto.DossierResponseDTO;

import java.util.List;

public interface DossierService {
    DossierResponseDTO creerDossier(DossierRequestDTO dto);
    DossierResponseDTO getDossierById(Long id);
    DossierResponseDTO getDossierByPatient(Long patientId);
    List<DossierResponseDTO> getAllDossiers(Long tenantId);
    DossierResponseDTO modifierDossier(Long id, DossierRequestDTO dto);
}
