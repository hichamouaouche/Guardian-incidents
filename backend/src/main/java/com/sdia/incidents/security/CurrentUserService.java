package com.sdia.incidents.security;

import com.sdia.incidents.entity.User;
import com.sdia.incidents.exception.ResourceNotFoundException;
import com.sdia.incidents.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Recupere l'utilisateur actuellement authentifie a partir du SecurityContextHolder.
 * Le principal place par le JwtAuthenticationFilter est l'entite User ;
 * on la recharge par id pour obtenir une entite "managed" dans la transaction courante.
 */
@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof User principal)) {
            throw new ResourceNotFoundException("Utilisateur introuvable");
        }
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
    }
}
