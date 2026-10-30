package com.sdia.incidents.dto;

import com.sdia.incidents.enums.Category;
import com.sdia.incidents.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record IncidentRequest(
        @NotBlank(message = "Le titre est obligatoire")
        @Size(min = 5, max = 150, message = "Le titre doit contenir entre 5 et 150 caracteres")
        String title,

        @NotBlank(message = "La description est obligatoire")
        @Size(min = 10, max = 2000, message = "La description doit contenir entre 10 et 2000 caracteres")
        String description,

        @NotNull(message = "La categorie est obligatoire")
        Category category,

        @NotNull(message = "La priorite est obligatoire")
        Priority priority
) {}
