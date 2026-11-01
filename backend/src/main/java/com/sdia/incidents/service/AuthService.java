package com.sdia.incidents.service;

import com.sdia.incidents.dto.*;
import com.sdia.incidents.entity.RefreshToken;
import com.sdia.incidents.entity.User;
import com.sdia.incidents.enums.Role;
import com.sdia.incidents.exception.EmailAlreadyUsedException;
import com.sdia.incidents.exception.InvalidTokenException;
import com.sdia.incidents.repository.RefreshTokenRepository;
import com.sdia.incidents.repository.UserRepository;
import com.sdia.incidents.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    /**
     * Inscription : le role est TOUJOURS EMPLOYEE (regle metier n°1 et n°2),
     * le mot de passe est encode avec BCrypt (regle n°3).
     */
    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyUsedException("Cet email est deja utilise");
        }
        User user = User.builder()
                .fullName(request.fullName().trim())
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .role(Role.EMPLOYEE)
                .enabled(true)
                .build();
        return UserResponse.from(userRepository.save(user));
    }

    /**
     * Connexion : l'AuthenticationManager verifie le mot de passe (BCrypt)
     * et le champ enabled (DisabledException si compte desactive -> regle n°7).
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(), request.password()));
        User user = (User) auth.getPrincipal();
        return buildAuthResponse(user, createRefreshToken(user));
    }

    /**
     * Renouvelle l'access token a partir d'un refresh token valide.
     * Rotation : l'ancien refresh token est revoque et un nouveau est emis.
     */
    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new InvalidTokenException("Refresh token invalide"));
        if (stored.isRevoked()) {
            throw new InvalidTokenException("Refresh token revoque");
        }
        if (stored.getExpiryDate().isBefore(Instant.now())) {
            throw new InvalidTokenException("Refresh token expire : veuillez vous reconnecter");
        }
        User user = stored.getUser();
        if (!user.isEnabled()) {
            throw new InvalidTokenException("Compte desactive");
        }
        stored.setRevoked(true);
        return buildAuthResponse(user, createRefreshToken(user));
    }

    /** Deconnexion : revocation du refresh token (l'access token expirera seul). */
    @Transactional
    public void logout(RefreshRequest request) {
        refreshTokenRepository.findByToken(request.refreshToken())
                .ifPresent(rt -> rt.setRevoked(true));
    }

    private RefreshToken createRefreshToken(User user) {
        RefreshToken rt = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryDate(Instant.now().plusMillis(refreshExpirationMs))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(rt);
    }

    private AuthResponse buildAuthResponse(User user, RefreshToken refreshToken) {
        return new AuthResponse(
                jwtService.generateToken(user),
                refreshToken.getToken(),
                "Bearer",
                jwtService.getAccessExpirationMs() / 1000,
                user.getRole().name(),
                user.getFullName(),
                user.getEmail());
    }
}
