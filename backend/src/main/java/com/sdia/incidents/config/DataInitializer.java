package com.sdia.incidents.config;

import com.sdia.incidents.entity.User;
import com.sdia.incidents.enums.Role;
import com.sdia.incidents.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Cree les comptes qui ne peuvent PAS etre obtenus par /auth/register
 * (ADMIN et TECHNICIAN), puisque l'inscription donne toujours le role EMPLOYEE.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        create("Hicham Ouaouche", "hichamouaouche@gmail.com", "Admin@2026", Role.ADMIN);
        create("Youssef Amrani", "tech1@incidents.ma", "Tech@2026", Role.TECHNICIAN);
        create("Karim Idrissi", "tech2@incidents.ma", "Tech@2026", Role.TECHNICIAN);
    }

    private void create(String name, String email, String rawPassword, Role role) {
        if (userRepository.existsByEmail(email)) return;
        userRepository.save(User.builder()
                .fullName(name)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .enabled(true)
                .build());
        log.info("Compte initial cree : {} ({})", email, role);
    }
}
