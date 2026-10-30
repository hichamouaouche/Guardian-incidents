package com.sdia.incidents.dto;

import com.sdia.incidents.entity.IncidentComment;

import java.time.LocalDateTime;

public record CommentResponse(Long id, String content, String author, String authorRole, LocalDateTime createdAt) {
    public static CommentResponse from(IncidentComment c) {
        return new CommentResponse(c.getId(), c.getContent(), c.getAuthor().getFullName(),
                c.getAuthor().getRole().name(), c.getCreatedAt());
    }
}
