package com.sdia.incidents.dto;

public record AuthResponse(
        String token,
        String refreshToken,
        String tokenType,
        long expiresIn,
        String role,
        String fullName,
        String email
) {}
