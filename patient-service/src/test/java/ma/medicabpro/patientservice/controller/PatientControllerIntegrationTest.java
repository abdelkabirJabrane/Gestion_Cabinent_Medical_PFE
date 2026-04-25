package ma.medicabpro.patientservice.controller;

import ma.medicabpro.patientservice.dto.PatientRequestDTO;
import ma.medicabpro.patientservice.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class PatientControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private PatientRepository patientRepository;

    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        patientRepository.deleteAll();
        restTemplate = new RestTemplate();
    }

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    void shouldCreerPatient() {
        PatientRequestDTO dto = new PatientRequestDTO();
        dto.setNom("Doe");
        dto.setPrenom("John");
        dto.setCin("AB123456");
        dto.setEmail("john.doe@example.com");
        dto.setTelephone("0612345678");
        dto.setSexe("MASCULIN");
        dto.setTenantId(1L);

        ResponseEntity<Object> response = restTemplate.postForEntity(getBaseUrl() + "/api/patients", dto, Object.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void shouldGetPatientByCin() {
        try {
            restTemplate.getForEntity(getBaseUrl() + "/api/patients/cin/AB123456", Object.class);
        } catch (HttpClientErrorException e) {
            assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
        }
    }
}
