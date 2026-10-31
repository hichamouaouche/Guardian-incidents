package com.sdia.incidents.security;

import com.sdia.incidents.entity.User;
import com.sdia.incidents.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Filtre execute une fois par requete :
 *  1. lit l'en-tete "Authorization: Bearer <JWT>"
 *  2. verifie signature + expiration
 *  3. recharge l'utilisateur en base (pour verifier qu'il existe toujours et qu'il est actif)
 *  4. place l'Authentication dans le SecurityContextHolder
 *
 * En cas d'erreur, la raison est stockee dans un attribut de requete ;
 * le JwtAuthenticationEntryPoint renverra ensuite un 401 avec un message precis.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String JWT_ERROR_ATTRIBUTE = "jwt_error";

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        try {
            Claims claims = jwtService.parseClaims(token);
            String email = claims.getSubject();

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                Optional<User> userOpt = userRepository.findByEmail(email);
                if (userOpt.isEmpty()) {
                    request.setAttribute(JWT_ERROR_ATTRIBUTE, "Utilisateur du token introuvable");
                } else if (!userOpt.get().isEnabled()) {
                    request.setAttribute(JWT_ERROR_ATTRIBUTE, "Compte desactive");
                } else {
                    User user = userOpt.get();
                    var authentication = new UsernamePasswordAuthenticationToken(
                            user, null, user.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (ExpiredJwtException e) {
            request.setAttribute(JWT_ERROR_ATTRIBUTE, "Token expire");
        } catch (JwtException | IllegalArgumentException e) {
            request.setAttribute(JWT_ERROR_ATTRIBUTE, "Token invalide");
        }

        filterChain.doFilter(request, response);
    }
}
