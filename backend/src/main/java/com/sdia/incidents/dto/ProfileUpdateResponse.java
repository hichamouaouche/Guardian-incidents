package com.sdia.incidents.dto;

/**
 * Reponse a la modification du profil.
 * Si l'email change, le "subject" du JWT change aussi : un nouveau token est donc renvoye.
 */
public record ProfileUpdateResponse(UserResponse user, String token) {}
