package com.shop.user_service.entity;

import lombok.Data;

@Data
public class AuthResponse {

    private String access_token;
    private String token_type;
    private User user;

}
