package com.shop.user_service.service;


import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;

    public JwtService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String generateToken(String username, Map<String, Object> claims) {

        Instant now = Instant.now();

        JwtClaimsSet jwtClaimsSet = JwtClaimsSet.builder()
                .issuer("user-service")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(86400))
                .subject(username)
                .claims(c -> c.putAll(claims))
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(jwtClaimsSet))
                .getTokenValue();
    }
}
