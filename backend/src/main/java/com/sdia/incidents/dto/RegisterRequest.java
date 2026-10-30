package com.sdia.incidents.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Donnees d'inscription.
 * Aucun champ "role" : l'utilisateur ne peut pas choisir son role.
 * Si le client envoie "role":"ADMIN" dans le JSON, le champ est simplement ignore.
 */
public record RegisterRequest(
        @NotBlank(message = "Le nom complet est obligatoire")
        @Size(min = 3, max = 100, message = "Le nom complet doit contenir entre 3 et 100 caracteres")
        String fullName,

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "L'email n'est pas valide")
        String email,

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 8, max = 72, message = "Le mot de passe doit contenir entre 8 et 72 caracteres")
        String password
) {}
