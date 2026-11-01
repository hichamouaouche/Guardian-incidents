package com.sdia.incidents.controller;

import com.sdia.incidents.dto.*;
import com.sdia.incidents.enums.Status;
import com.sdia.incidents.service.IncidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Securisation a 2 niveaux :
 *  1. @PreAuthorize : verifie le ROLE avant d'entrer dans la methode
 *  2. IncidentService : verifie la PROPRIETE / l'AFFECTATION de l'incident
 */
@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('EMPLOYEE')")
    public IncidentResponse create(@Valid @RequestBody IncidentRequest request) {
        return incidentService.create(request);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public List<IncidentResponse> myIncidents() {
        return incidentService.myIncidents();
    }

    @GetMapping("/assigned")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public List<IncidentResponse> assigned() {
        return incidentService.assignedIncidents();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<IncidentResponse> all(@RequestParam(required = false) Status status) {
        return incidentService.allIncidents(status);
    }

    /** Acces selon proprietaire (EMPLOYEE), affectation (TECHNICIAN) ou role (ADMIN). */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public IncidentDetailsResponse getById(@PathVariable Long id) {
        return incidentService.getById(id);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('TECHNICIAN','ADMIN')")
    public IncidentResponse updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        return incidentService.updateStatus(id, request);
    }

    @PutMapping("/{id}/assign/{technicianId}")
    @PreAuthorize("hasRole('ADMIN')")
    public IncidentResponse assign(@PathVariable Long id, @PathVariable Long technicianId) {
        return incidentService.assign(id, technicianId);
    }

    @PostMapping("/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('TECHNICIAN','ADMIN')")
    public CommentResponse addComment(@PathVariable Long id, @Valid @RequestBody CommentRequest request) {
        return incidentService.addComment(id, request);
    }
}
