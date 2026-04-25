package ma.medicabpro.ordonnanceservice.controller;

import ma.medicabpro.ordonnanceservice.dto.OrdonnanceRequestDTO;
import ma.medicabpro.ordonnanceservice.repository.OrdonnanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class OrdonnanceControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private OrdonnanceRepository ordonnanceRepository;

    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        ordonnanceRepository.deleteAll();
        restTemplate = new RestTemplate();
    }

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    void shouldCreerOrdonnance() {
        OrdonnanceRequestDTO dto = new OrdonnanceRequestDTO();
        dto.setPatientId(1L);
        dto.setMedecinId(1L);
        dto.setConsultationId(1L);
        dto.setTenantId(1L);
        dto.setStatut("ACTIVE");
        dto.setLignes(new ArrayList<>());

        ResponseEntity<Object> response = restTemplate.postForEntity(getBaseUrl() + "/api/ordonnances", dto, Object.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void shouldGetAllOrdonnancesByTenant() {
        ResponseEntity<Object> response = restTemplate.getForEntity(getBaseUrl() + "/api/ordonnances?tenantId=1", Object.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
