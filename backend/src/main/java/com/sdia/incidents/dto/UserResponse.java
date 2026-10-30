package com.sdia.incidents.dto;

import com.sdia.incidents.entity.User;

import java.time.LocalDateTime;

/** Representation publique d'un utilisateur : le mot de passe n'est JAMAIS renvoye. */
public record UserResponse(
        Long id,
        String fullName,
        String email,
        String role,
        boolean enabled,
        LocalDateTime createdAt
) {
    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getFullName(), u.getEmail(),
                u.getRole().name(), u.isEnabled(), u.getCreatedAt());
    }
}
