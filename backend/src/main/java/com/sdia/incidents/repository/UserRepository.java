package com.sdia.incidents.repository;

import com.sdia.incidents.entity.User;
import com.sdia.incidents.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    long countByRole(Role role);
    List<User> findByRoleAndEnabledTrueOrderByFullNameAsc(Role role);
    List<User> findAllByOrderByIdAsc();
}
