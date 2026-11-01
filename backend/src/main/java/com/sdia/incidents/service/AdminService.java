package com.sdia.incidents.service;

import com.sdia.incidents.dto.UserResponse;
import com.sdia.incidents.entity.User;
import com.sdia.incidents.enums.Role;
import com.sdia.incidents.enums.Status;
import com.sdia.incidents.exception.BadRequestException;
import com.sdia.incidents.exception.ResourceNotFoundException;
import com.sdia.incidents.repository.IncidentRepository;
import com.sdia.incidents.repository.RefreshTokenRepository;
import com.sdia.incidents.repository.UserRepository;
import com.sdia.incidents.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final IncidentRepository incidentRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public List<UserResponse> allUsers() {
        return userRepository.findAllByOrderByIdAsc().stream().map(UserResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> technicians() {
        return userRepository.findByRoleAndEnabledTrueOrderByFullNameAsc(Role.TECHNICIAN)
                .stream().map(UserResponse::from).toList();
    }

    @Transactional
    public UserResponse setEnabled(Long userId, boolean enabled) {
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        User me = currentUserService.getCurrentUser();
        if (target.getId().equals(me.getId()) && !enabled) {
            throw new BadRequestException("Un administrateur ne peut pas desactiver son propre compte");
        }
        target.setEnabled(enabled);
        if (!enabled) {
            // un compte desactive perd immediatement ses refresh tokens
            refreshTokenRepository.revokeAllForUser(target);
        }
        return UserResponse.from(userRepository.save(target));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> statistics() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("employees", userRepository.countByRole(Role.EMPLOYEE));
        stats.put("technicians", userRepository.countByRole(Role.TECHNICIAN));
        stats.put("admins", userRepository.countByRole(Role.ADMIN));
        stats.put("totalIncidents", incidentRepository.count());
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (Status s : Status.values()) {
            byStatus.put(s.name(), incidentRepository.countByStatus(s));
        }
        stats.put("open", byStatus.get("OPEN"));
        stats.put("inProgress", byStatus.get("IN_PROGRESS"));
        stats.put("closed", byStatus.get("CLOSED"));
        stats.put("byStatus", byStatus);
        return stats;
    }
}
