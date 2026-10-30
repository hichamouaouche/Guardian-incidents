package com.sdia.incidents.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentRequest(
        @NotBlank(message = "Le contenu de la note est obligatoire")
        @Size(max = 1000, message = "La note ne doit pas depasser 1000 caracteres")
        String content
) {}
