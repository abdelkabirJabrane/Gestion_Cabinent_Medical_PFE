package ma.medicabpro.appointmentservice.service;


import ma.medicabpro.appointmentservice.dto.AppointmentRequestDTO;
import ma.medicabpro.appointmentservice.dto.AppointmentResponseDTO;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentService {

    AppointmentResponseDTO creerRDV(
            AppointmentRequestDTO dto);

    AppointmentResponseDTO getRDVById(Long id);

    List<AppointmentResponseDTO> getRDVByPatient(
            Long patientId);

    List<AppointmentResponseDTO> getRDVByMedecin(
            Long medecinId);

    List<AppointmentResponseDTO> getRDVDuJour(
            Long medecinId, LocalDate date);

    List<AppointmentResponseDTO> getRDVByTenant(
            Long tenantId);

    AppointmentResponseDTO confirmerRDV(Long id);

    AppointmentResponseDTO annulerRDV(
            Long id, String motif);

    AppointmentResponseDTO terminerRDV(Long id);

    AppointmentResponseDTO enCoursRDV(Long id);

    AppointmentResponseDTO marquerAbsent(Long id);

    void supprimerRDV(Long id);

    long countRDV(Long tenantId);

    List<String> getClosedDays(Long medecinId, Long tenantId);

    /** Pour la vue publique patient - pas de filtre tenantId */
    List<String> getPublicClosedDays(Long medecinId);

    void toggleClosedDay(ma.medicabpro.appointmentservice.dto.ToggleClosedDayRequest request);
}