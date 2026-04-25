package com.shop.user_service.controller;

import com.shop.user_service.entity.RegisterRequest;
import com.shop.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AuthControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        restTemplate = new RestTemplate();
    }

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    void shouldRegisterUser() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("Password123!");
        request.setEmail("test@example.com");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setRole("PATIENT");

        ResponseEntity<Map> response = restTemplate.postForEntity(getBaseUrl() + "/api/auth/register", request, Map.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody().get("access_token"));
    }

    @Test
    void shouldLoginUser() {
        // First register
        RegisterRequest request = new RegisterRequest();
        request.setUsername("loginuser");
        request.setPassword("Password123!");
        request.setEmail("login@example.com");
        request.setFirstName("Login");
        request.setLastName("User");
        request.setRole("PATIENT");
        restTemplate.postForEntity(getBaseUrl() + "/api/auth/register", request, Map.class);

        // Then login
        ResponseEntity<Map> response = restTemplate.postForEntity(getBaseUrl() + "/api/auth/login?username=loginuser&password=Password123!", null, Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().get("access_token"));
    }
}
