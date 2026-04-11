package com.shop.user_service.controller;

import com.shop.user_service.entity.AuthResponse;
import com.shop.user_service.entity.Role;
import com.shop.user_service.entity.RegisterRequest;
import com.shop.user_service.entity.User;
import com.shop.user_service.repository.UserRepository;
import com.shop.user_service.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    // ← PAS BESOIN de RoleRepository car Role est un enum

    @PostMapping("/login")
    public AuthResponse login(
            @RequestParam String username,
            @RequestParam String password
    ) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtService.generateToken(
                user.getUsername(),
                Map.of("roles", user.getRoles())
        );

        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccess_token(token);
        authResponse.setToken_type("Bearer");
        authResponse.setUser(user);

        return authResponse;

    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            // Vérifier si l'email existe déjà
            if (userRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Email is already in use"));
            }

            // Vérifier si le username existe déjà
            if (userRepository.existsByUsername(request.getUsername())) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Username is already taken"));
            }

            // Créer le nouvel utilisateur
            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setPhoneNumber(request.getPhoneNumber());
            user.setAddress(request.getAddress());
            user.setGender(request.getGender());

            // Assigner le rôle (depuis la requête ou PATIENT par défaut)
            Set<Role> roles = new HashSet<>();
            if (request.getRole() != null && !request.getRole().isEmpty()) {
                try {
                    String roleStr = request.getRole().toUpperCase();
                    if (!roleStr.startsWith("ROLE_")) {
                        roleStr = "ROLE_" + roleStr;
                    }
                    roles.add(Role.valueOf(roleStr));
                } catch (IllegalArgumentException e) {
                    roles.add(Role.ROLE_PATIENT);
                }
            } else {
                roles.add(Role.ROLE_PATIENT);
            }
            user.setRoles(roles);
            user.setMedecinId(request.getMedecinId());

            user.setActive(true);
            user.setCreatedAt(LocalDateTime.now());

            // Sauvegarder l'utilisateur
            User savedUser = userRepository.save(user);

            // Générer le token JWT
            String token = jwtService.generateToken(
                    savedUser.getUsername(),
                    Map.of("roles", savedUser.getRoles())
            );

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", savedUser.getId());
            userInfo.put("username", savedUser.getUsername());
            userInfo.put("email", savedUser.getEmail());
            userInfo.put("firstName", savedUser.getFirstName());
            userInfo.put("lastName", savedUser.getLastName());
            userInfo.put("roles", savedUser.getRoles());
            if (savedUser.getMedecinId() != null) {
                userInfo.put("medecinId", savedUser.getMedecinId());
            }

            // Retourner la réponse avec le token et les infos utilisateur
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "access_token", token,
                    "token_type", "Bearer",
                    "user", userInfo
            ));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Registration failed: " + e.getMessage()));
        }
    }
}