package com.sdia.incidents.dto;

import com.sdia.incidents.entity.User;

public record UserSummary(Long id, String fullName, String email) {
    public static UserSummary from(User u) {
        return u == null ? null : new UserSummary(u.getId(), u.getFullName(), u.getEmail());
    }
}
