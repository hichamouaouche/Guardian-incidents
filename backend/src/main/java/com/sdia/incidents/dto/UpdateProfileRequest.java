package com.sdia.incidents.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank(message = "Le nom complet est obligatoire")
        @Size(min = 3, max = 100, message = "Le nom complet doit contenir entre 3 et 100 caracteres")
        String fullName,

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "L'email n'est pas valide")
        String email
) {}
