package com.sdia.incidents.dto;

import com.sdia.incidents.entity.Incident;

import java.time.LocalDateTime;

/** Vue "liste" d'un incident. */
public record IncidentResponse(
        Long id,
        String title,
        String description,
        String category,
        String priority,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        UserSummary createdBy,
        UserSummary assignedTo
) {
    public static IncidentResponse from(Incident i) {
        return new IncidentResponse(i.getId(), i.getTitle(), i.getDescription(),
                i.getCategory().name(), i.getPriority().name(), i.getStatus().name(),
                i.getCreatedAt(), i.getUpdatedAt(),
                UserSummary.from(i.getCreatedBy()), UserSummary.from(i.getAssignedTo()));
    }
}
