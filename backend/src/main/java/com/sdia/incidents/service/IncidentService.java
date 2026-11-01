package com.sdia.incidents.service;

import com.sdia.incidents.dto.*;
import com.sdia.incidents.entity.Incident;
import com.sdia.incidents.entity.IncidentComment;
import com.sdia.incidents.entity.IncidentHistory;
import com.sdia.incidents.entity.User;
import com.sdia.incidents.enums.Role;
import com.sdia.incidents.enums.Status;
import com.sdia.incidents.exception.BadRequestException;
import com.sdia.incidents.exception.BusinessRuleException;
import com.sdia.incidents.exception.ForbiddenOperationException;
import com.sdia.incidents.exception.ResourceNotFoundException;
import com.sdia.incidents.repository.IncidentCommentRepository;
import com.sdia.incidents.repository.IncidentRepository;
import com.sdia.incidents.repository.UserRepository;
import com.sdia.incidents.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class IncidentService {

    /** Transitions de statut autorisees (machine a etats). CLOSED est un etat final. */
    private static final Map<Status, Set<Status>> TRANSITIONS = Map.of(
            Status.OPEN,        EnumSet.of(Status.IN_PROGRESS, Status.CLOSED),
            Status.ASSIGNED,    EnumSet.of(Status.IN_PROGRESS, Status.CLOSED),
            Status.IN_PROGRESS, EnumSet.of(Status.RESOLVED, Status.CLOSED),
            Status.RESOLVED,    EnumSet.of(Status.IN_PROGRESS, Status.CLOSED),
            Status.CLOSED,      EnumSet.noneOf(Status.class)
    );

    /** Statuts qu'un technicien a le droit de positionner. */
    private static final Set<Status> TECHNICIAN_TARGETS = EnumSet.of(Status.IN_PROGRESS, Status.RESOLVED);

    private final IncidentRepository incidentRepository;
    private final IncidentCommentRepository commentRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    // ------------------------------------------------------------------ creation
    @Transactional
    public IncidentResponse create(IncidentRequest request) {
        User me = currentUserService.getCurrentUser();
        Incident incident = Incident.builder()
                .title(request.title().trim())
                .description(request.description().trim())
                .category(request.category())
                .priority(request.priority())
                .status(Status.OPEN)
                .createdBy(me)
                .build();
        addHistory(incident, "CREATED", "Incident cree par " + me.getFullName(), null, Status.OPEN, me);
        return IncidentResponse.from(incidentRepository.save(incident));
    }

    // ------------------------------------------------------------------ lectures
    @Transactional(readOnly = true)
    public List<IncidentResponse> myIncidents() {
        User me = currentUserService.getCurrentUser();
        return incidentRepository.findByCreatedByOrderByCreatedAtDesc(me)
                .stream().map(IncidentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<IncidentResponse> assignedIncidents() {
        User me = currentUserService.getCurrentUser();
        return incidentRepository.findByAssignedToOrderByCreatedAtDesc(me)
                .stream().map(IncidentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<IncidentResponse> allIncidents(Status status) {
        return incidentRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(i -> status == null || i.getStatus() == status)
                .map(IncidentResponse::from).toList();
    }

    /**
     * Detail d'un incident avec CONTROLE DU PROPRIETAIRE :
     *  - ADMIN      : tous les incidents
     *  - EMPLOYEE   : uniquement les incidents qu'il a crees
     *  - TECHNICIAN : uniquement les incidents qui lui sont affectes
     * Le controle est fait cote serveur : masquer un bouton dans React ne suffit pas.
     */
    @Transactional(readOnly = true)
    public IncidentDetailsResponse getById(Long id) {
        User me = currentUserService.getCurrentUser();
        Incident incident = findIncident(id);
        checkReadAccess(incident, me);
        return IncidentDetailsResponse.from(incident);
    }

    // ------------------------------------------------------------------ statut
    @Transactional
    public IncidentResponse updateStatus(Long id, StatusUpdateRequest request) {
        User me = currentUserService.getCurrentUser();
        Incident incident = findIncident(id);
        Status current = incident.getStatus();
        Status target = request.status();

        // Regle n°5 : un technicien ne modifie que les incidents qui lui sont affectes
        if (me.getRole() == Role.TECHNICIAN && !isAssignedTo(incident, me)) {
            throw new ForbiddenOperationException(
                    "Acces interdit : cet incident ne vous est pas affecte");
        }
        // Regle n°8 : un incident CLOSED ne peut plus changer de statut
        if (current == Status.CLOSED) {
            throw new BusinessRuleException("Incident cloture : son statut ne peut plus etre modifie");
        }
        if (current == target) {
            throw new BadRequestException("L'incident est deja au statut " + target);
        }
        if (me.getRole() == Role.TECHNICIAN && !TECHNICIAN_TARGETS.contains(target)) {
            throw new ForbiddenOperationException(
                    "Un technicien ne peut positionner que IN_PROGRESS ou RESOLVED");
        }
        if (!TRANSITIONS.get(current).contains(target)) {
            throw new BusinessRuleException("Transition interdite : " + current + " -> " + target);
        }

        incident.setStatus(target);
        addHistory(incident, "STATUS_CHANGED", current + " -> " + target, current, target, me);
        return IncidentResponse.from(incidentRepository.save(incident));
    }

    // ------------------------------------------------------------------ affectation
    /** Regle n°6 : seul un ADMIN peut affecter (verifie aussi par @PreAuthorize). */
    @Transactional
    public IncidentResponse assign(Long id, Long technicianId) {
        User me = currentUserService.getCurrentUser();
        Incident incident = findIncident(id);
        if (incident.getStatus() == Status.CLOSED) {
            throw new BusinessRuleException("Incident cloture : il ne peut plus etre affecte");
        }
        User technician = userRepository.findById(technicianId)
                .orElseThrow(() -> new ResourceNotFoundException("Technicien introuvable"));
        if (technician.getRole() != Role.TECHNICIAN) {
            throw new BadRequestException("L'utilisateur choisi n'est pas un technicien");
        }
        if (!technician.isEnabled()) {
            throw new BadRequestException("Ce technicien est desactive");
        }
        if (isAssignedTo(incident, technician)) {
            throw new BadRequestException("L'incident est deja affecte a ce technicien");
        }

        incident.setAssignedTo(technician);
        Status old = incident.getStatus();
        if (old == Status.OPEN) {
            incident.setStatus(Status.ASSIGNED);
        }
        addHistory(incident, "ASSIGNED", "Affecte a " + technician.getFullName(),
                old, incident.getStatus(), me);
        return IncidentResponse.from(incidentRepository.save(incident));
    }

    // ------------------------------------------------------------------ notes
    @Transactional
    public CommentResponse addComment(Long id, CommentRequest request) {
        User me = currentUserService.getCurrentUser();
        Incident incident = findIncident(id);
        if (me.getRole() == Role.TECHNICIAN && !isAssignedTo(incident, me)) {
            throw new ForbiddenOperationException(
                    "Acces interdit : cet incident ne vous est pas affecte");
        }
        if (incident.getStatus() == Status.CLOSED) {
            throw new BusinessRuleException("Incident cloture : aucune note ne peut etre ajoutee");
        }
        IncidentComment comment = IncidentComment.builder()
                .incident(incident)
                .author(me)
                .content(request.content().trim())
                .build();
        incident.getComments().add(comment);
        incident.setUpdatedAt(java.time.LocalDateTime.now());
        return CommentResponse.from(commentRepository.save(comment));
    }

    // ------------------------------------------------------------------ outils
    private Incident findIncident(Long id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident introuvable"));
    }

    private boolean isAssignedTo(Incident incident, User user) {
        return incident.getAssignedTo() != null && incident.getAssignedTo().getId().equals(user.getId());
    }

    private void checkReadAccess(Incident incident, User me) {
        boolean allowed = switch (me.getRole()) {
            case ADMIN -> true;
            case EMPLOYEE -> incident.getCreatedBy().getId().equals(me.getId());
            case TECHNICIAN -> isAssignedTo(incident, me);
        };
        if (!allowed) {
            throw new ForbiddenOperationException(
                    "Acces interdit : vous n'avez pas le droit de consulter cet incident");
        }
    }

    private void addHistory(Incident incident, String action, String description,
                            Status oldStatus, Status newStatus, User by) {
        incident.getHistory().add(IncidentHistory.builder()
                .incident(incident)
                .action(action)
                .description(description)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .performedBy(by)
                .build());
    }
}
