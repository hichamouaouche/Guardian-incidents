package com.sdia.incidents.service;

import com.sdia.incidents.dto.IncidentResponse;
import com.sdia.incidents.entity.User;
import com.sdia.incidents.enums.Priority;
import com.sdia.incidents.enums.Status;
import com.sdia.incidents.repository.IncidentRepository;
import com.sdia.incidents.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Statistiques personnelles du dashboard, calculees selon le role connecte. */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CurrentUserService currentUserService;
    private final IncidentRepository incidentRepository;
    private final AdminService adminService;

    @Transactional(readOnly = true)
    public Map<String, Object> dashboard() {
        User me = currentUserService.getCurrentUser();
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("role", me.getRole().name());
        switch (me.getRole()) {
            case EMPLOYEE -> {
                d.put("total", incidentRepository.countByCreatedBy(me));
                d.put("open", incidentRepository.countByCreatedByAndStatusIn(me, List.of(Status.OPEN, Status.ASSIGNED)));
                d.put("inProgress", incidentRepository.countByCreatedByAndStatus(me, Status.IN_PROGRESS));
                d.put("resolvedOrClosed", incidentRepository.countByCreatedByAndStatusIn(me, List.of(Status.RESOLVED, Status.CLOSED)));
                d.put("latest", incidentRepository.findByCreatedByOrderByCreatedAtDesc(me).stream()
                        .limit(5).map(IncidentResponse::from).toList());
            }
            case TECHNICIAN -> {
                d.put("assigned", incidentRepository.countByAssignedTo(me));
                d.put("critical", incidentRepository.countByAssignedToAndPriority(me, Priority.CRITICAL));
                d.put("inProgress", incidentRepository.countByAssignedToAndStatus(me, Status.IN_PROGRESS));
                d.put("resolved", incidentRepository.countByAssignedToAndStatusIn(me, List.of(Status.RESOLVED, Status.CLOSED)));
                d.put("latest", incidentRepository.findByAssignedToOrderByCreatedAtDesc(me).stream()
                        .limit(5).map(IncidentResponse::from).toList());
            }
            case ADMIN -> d.putAll(adminService.statistics());
        }
        return d;
    }
}
