package com.sdia.incidents.service;

import com.sdia.incidents.dto.*;
import com.sdia.incidents.entity.User;
import com.sdia.incidents.exception.BadRequestException;
import com.sdia.incidents.exception.EmailAlreadyUsedException;
import com.sdia.incidents.repository.UserRepository;
import com.sdia.incidents.security.CurrentUserService;
import com.sdia.incidents.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public UserResponse getProfile() {
        return UserResponse.from(currentUserService.getCurrentUser());
    }

    /** Modification du nom et de l'email (le role ne peut PAS etre modifie ici). */
    @Transactional
    public ProfileUpdateResponse updateProfile(UpdateProfileRequest request) {
        User user = currentUserService.getCurrentUser();
        String newEmail = request.email().trim().toLowerCase();
        if (!newEmail.equals(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
            throw new EmailAlreadyUsedException("Cet email est deja utilise");
        }
        user.setFullName(request.fullName().trim());
        user.setEmail(newEmail);
        userRepository.save(user);
        // nouveau token car le "sub" (email) a pu changer
        return new ProfileUpdateResponse(UserResponse.from(user), jwtService.generateToken(user));
    }

    /** Changement de mot de passe : l'ancien mot de passe est verifie avant tout. */
    @Transactional
    public MessageResponse changePassword(ChangePasswordRequest request) {
        User user = currentUserService.getCurrentUser();
        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new BadRequestException("Ancien mot de passe incorrect");
        }
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BadRequestException("La confirmation ne correspond pas au nouveau mot de passe");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new BadRequestException("Le nouveau mot de passe doit etre different de l'ancien");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        return new MessageResponse("Mot de passe modifie avec succes");
    }
}
