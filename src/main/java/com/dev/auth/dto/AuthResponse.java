package com.dev.auth.dto;

import lombok.Data;

@Data
public class AuthResponse {

    private String id;
    private String username;
    private String email;
    private String token;
    // thêm refresh token
    private String refreshToken;
}
