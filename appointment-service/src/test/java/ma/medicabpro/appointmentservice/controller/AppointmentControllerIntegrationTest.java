package ma.medicabpro.appointmentservice.controller;

import ma.medicabpro.appointmentservice.dto.AppointmentRequestDTO;
import ma.medicabpro.appointmentservice.repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AppointmentControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private AppointmentRepository appointmentRepository;

    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        appointmentRepository.deleteAll();
        restTemplate = new RestTemplate();
    }

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    void shouldCreerRendezVous() {
        AppointmentRequestDTO dto = new AppointmentRequestDTO();
        dto.setPatientId(1L);
        dto.setMedecinId(1L);
        dto.setTenantId(1L);
        dto.setDateHeureDebut(LocalDateTime.now().plusDays(1));
        dto.setMotif("Consultation initiale");

        ResponseEntity<Object> response = restTemplate.postForEntity(getBaseUrl() + "/api/appointments", dto, Object.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void shouldGetPublicAppointments() {
        ResponseEntity<Object> response = restTemplate.getForEntity(getBaseUrl() + "/api/appointments/public/medecin/1", Object.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
