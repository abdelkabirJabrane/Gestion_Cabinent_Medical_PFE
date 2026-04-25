package ma.medicabpro.medicalrecordservice.controller;

import ma.medicabpro.medicalrecordservice.dto.DossierRequestDTO;
import ma.medicabpro.medicalrecordservice.repository.DossierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class DossierControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DossierRepository dossierRepository;

    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        dossierRepository.deleteAll();
        restTemplate = new RestTemplate();
    }

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    void shouldCreerDossier() {
        DossierRequestDTO dto = new DossierRequestDTO();
        dto.setPatientId(1L);
        dto.setTenantId(1L);

        ResponseEntity<Object> response = restTemplate.postForEntity(getBaseUrl() + "/api/dossiers", dto, Object.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void shouldGetAllByTenant() {
        ResponseEntity<Object> response = restTemplate.getForEntity(getBaseUrl() + "/api/dossiers?tenantId=1", Object.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
