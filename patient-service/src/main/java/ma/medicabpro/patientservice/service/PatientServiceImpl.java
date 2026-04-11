package ma.medicabpro.patientservice.service;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import ma.medicabpro.patientservice.dto.PatientRequestDTO;
import ma.medicabpro.patientservice.dto.PatientResponseDTO;
import ma.medicabpro.patientservice.entity.Patient;
import ma.medicabpro.patientservice.entity.enums.Sexe;
import ma.medicabpro.patientservice.entity.enums.TypeAssurance;
import ma.medicabpro.patientservice.exception.PatientNotFoundException;
import ma.medicabpro.patientservice.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation
        .Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PatientServiceImpl
        implements PatientService {

    private final PatientRepository patientRepository;

    // ── Créer patient ──────────────────────
    @Override
    public PatientResponseDTO creerPatient(
            PatientRequestDTO dto) {

        log.info("Création patient CIN: {}",
                dto.getCin());

        // Vérifications unicité
        if (patientRepository.existsByCin(
                dto.getCin())) {
            throw new RuntimeException(
                    "CIN déjà existant : " + dto.getCin());
        }
        if (patientRepository.existsByEmail(
                dto.getEmail())) {
            throw new RuntimeException(
                    "Email déjà utilisé : " + dto.getEmail());
        }

        Patient patient = Patient.builder()
                .tenantId(dto.getTenantId())
                .nom(dto.getNom().toUpperCase())
                .prenom(dto.getPrenom())
                .cin(dto.getCin().toUpperCase())
                .email(dto.getEmail().toLowerCase())
                .telephone(dto.getTelephone())
                .dateNaissance(dto.getDateNaissance())
                .sexe(dto.getSexe() != null
                        ? Sexe.valueOf(dto.getSexe()) : null)
                .groupeSanguin(dto.getGroupeSanguin())
                .typeAssurance(dto.getTypeAssurance() != null
                        ? TypeAssurance.valueOf(
                        dto.getTypeAssurance()) : null)
                .noAffiliation(dto.getNoAffiliation())
                .poids(dto.getPoids())
                .taille(dto.getTaille())
                .ville(dto.getVille())
                .adresse(dto.getAdresse())
                .build();

        Patient saved = patientRepository.save(patient);
        log.info("Patient créé avec ID: {}",
                saved.getId());
        return toDTO(saved);
    }

    // ── Récupérer par ID ───────────────────
    @Override
    @Transactional(readOnly = true)
    public PatientResponseDTO getPatientById(Long id) {
        Patient patient = patientRepository
                .findById(id)
                .orElseThrow(() ->
                        new PatientNotFoundException(
                                "Patient introuvable : " + id));
        return toDTO(patient);
    }

    // ── Récupérer par CIN ──────────────────
    @Override
    @Transactional(readOnly = true)
    public PatientResponseDTO getPatientByCin(
            String cin) {
        Patient patient = patientRepository
                .findByCin(cin.toUpperCase())
                .orElseThrow(() ->
                        new PatientNotFoundException(
                                "Patient introuvable CIN : " + cin));
        return toDTO(patient);
    }

    // ── Liste patients du cabinet ──────────
    @Override
    @Transactional(readOnly = true)
    public List<PatientResponseDTO> getAllPatients(
            Long tenantId) {
        return patientRepository
                .findByTenantIdAndActifTrue(tenantId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ── Recherche ──────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<PatientResponseDTO> rechercherPatients(
            Long tenantId, String query) {
        return patientRepository
                .rechercherPatients(tenantId, query)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ── Modifier patient ───────────────────
    @Override
    public PatientResponseDTO modifierPatient(
            Long id, PatientRequestDTO dto) {

        Patient patient = patientRepository
                .findById(id)
                .orElseThrow(() ->
                        new PatientNotFoundException(
                                "Patient introuvable : " + id));

        patient.setNom(dto.getNom().toUpperCase());
        patient.setPrenom(dto.getPrenom());
        patient.setEmail(
                dto.getEmail().toLowerCase());
        patient.setTelephone(dto.getTelephone());
        patient.setDateNaissance(
                dto.getDateNaissance());
        patient.setGroupeSanguin(
                dto.getGroupeSanguin());
        patient.setPoids(dto.getPoids());
        patient.setTaille(dto.getTaille());
        patient.setVille(dto.getVille());
        patient.setAdresse(dto.getAdresse());

        if (dto.getTypeAssurance() != null) {
            patient.setTypeAssurance(
                    TypeAssurance.valueOf(
                            dto.getTypeAssurance()));
        }

        return toDTO(patientRepository.save(patient));
    }

    // ── Désactiver (soft delete) ───────────
    @Override
    public void desactiverPatient(Long id) {
        Patient patient = patientRepository
                .findById(id)
                .orElseThrow(() ->
                        new PatientNotFoundException(
                                "Patient introuvable : " + id));
        patient.setActif(false);
        patientRepository.save(patient);
        log.info("Patient {} désactivé", id);
    }

    // ── Statistiques ───────────────────────
    @Override
    @Transactional(readOnly = true)
    public long countPatients(Long tenantId) {
        return patientRepository
                .countByTenantIdAndActifTrue(tenantId);
    }

    // ── Mapper Entity → DTO ────────────────
    private PatientResponseDTO toDTO(Patient p) {

        // Catégorie IMC
        double imc = p.getIMC();
        String categorieIMC = "";
        if (imc > 0) {
            if (imc < 18.5)
                categorieIMC = "Insuffisance pondérale";
            else if (imc < 25)
                categorieIMC = "Poids normal";
            else if (imc < 30)
                categorieIMC = "Surpoids";
            else
                categorieIMC = "Obésité";
        }

        return PatientResponseDTO.builder()
                .id(p.getId())
                .tenantId(p.getTenantId())
                .nom(p.getNom())
                .prenom(p.getPrenom())
                .nomComplet(p.getNomComplet())
                .cin(p.getCin())
                .email(p.getEmail())
                .telephone(p.getTelephone())
                .dateNaissance(p.getDateNaissance())
                .age(p.getAge())
                .sexe(p.getSexe() != null
                        ? p.getSexe().name() : null)
                .groupeSanguin(p.getGroupeSanguin())
                .typeAssurance(p.getTypeAssurance() != null
                        ? p.getTypeAssurance().name() : null)
                .noAffiliation(p.getNoAffiliation())
                .poids(p.getPoids())
                .taille(p.getTaille())
                .imc(imc)
                .categorieIMC(categorieIMC)
                .ville(p.getVille())
                .adresse(p.getAdresse())
                .dateCreation(p.getDateCreation())
                .dateModification(p.getDateModification())
                .actif(p.isActif())
                .build();
    }
}