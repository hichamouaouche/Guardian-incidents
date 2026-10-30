package com.sdia.incidents.dto;

import com.sdia.incidents.entity.IncidentHistory;

import java.time.LocalDateTime;

public record HistoryResponse(
        Long id,
        String action,
        String description,
        String oldStatus,
        String newStatus,
        String performedBy,
        LocalDateTime createdAt
) {
    public static HistoryResponse from(IncidentHistory h) {
        return new HistoryResponse(h.getId(), h.getAction(), h.getDescription(),
                h.getOldStatus() == null ? null : h.getOldStatus().name(),
                h.getNewStatus() == null ? null : h.getNewStatus().name(),
                h.getPerformedBy().getFullName(), h.getCreatedAt());
    }
}
