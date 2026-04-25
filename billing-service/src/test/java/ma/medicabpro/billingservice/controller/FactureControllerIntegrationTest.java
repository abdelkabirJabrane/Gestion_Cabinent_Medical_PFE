package ma.medicabpro.billingservice.controller;

import ma.medicabpro.billingservice.dto.FactureRequestDTO;
import ma.medicabpro.billingservice.repository.FactureRepository;
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
public class FactureControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private FactureRepository factureRepository;

    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        factureRepository.deleteAll();
        restTemplate = new RestTemplate();
    }

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    void shouldCreerFacture() {
        FactureRequestDTO dto = new FactureRequestDTO();
        dto.setPatientId(1L);
        dto.setTenantId(1L);
        dto.setMontantTTC(100.0);
        dto.setConsultationId(1L);

        ResponseEntity<Object> response = restTemplate.postForEntity(getBaseUrl() + "/api/factures", dto, Object.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void shouldGetStats() {
        ResponseEntity<Object> response = restTemplate.getForEntity(getBaseUrl() + "/api/factures/stats/encaisse?tenantId=1", Object.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
