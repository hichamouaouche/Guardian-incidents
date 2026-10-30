package com.sdia.incidents.dto;

import com.sdia.incidents.enums.Status;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(
        @NotNull(message = "Le statut est obligatoire")
        Status status
) {}
