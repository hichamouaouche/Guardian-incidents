package com.sdia.incidents.controller;

import com.sdia.incidents.dto.UserResponse;
import com.sdia.incidents.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * /api/admin/** : protege 2 fois
 *  - dans SecurityConfig : requestMatchers("/api/admin/**").hasRole("ADMIN")
 *  - ici : @PreAuthorize("hasRole('ADMIN')") (defense en profondeur)
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public List<UserResponse> users() {
        return adminService.allUsers();
    }

    @GetMapping("/technicians")
    public List<UserResponse> technicians() {
        return adminService.technicians();
    }

    @PutMapping("/users/{id}/enable")
    public UserResponse enable(@PathVariable Long id) {
        return adminService.setEnabled(id, true);
    }

    @PutMapping("/users/{id}/disable")
    public UserResponse disable(@PathVariable Long id) {
        return adminService.setEnabled(id, false);
    }

    @GetMapping("/statistics")
    public Map<String, Object> statistics() {
        return adminService.statistics();
    }
}
