package com.sdia.incidents.repository;

import com.sdia.incidents.entity.Incident;
import com.sdia.incidents.entity.User;
import com.sdia.incidents.enums.Priority;
import com.sdia.incidents.enums.Status;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    // @EntityGraph : charge createdBy/assignedTo en une seule requete (evite le probleme N+1
    // tout en gardant LAZY comme strategie par defaut sur l'entite)
    @EntityGraph(attributePaths = {"createdBy", "assignedTo"})
    List<Incident> findByCreatedByOrderByCreatedAtDesc(User createdBy);

    @EntityGraph(attributePaths = {"createdBy", "assignedTo"})
    List<Incident> findByAssignedToOrderByCreatedAtDesc(User assignedTo);

    @EntityGraph(attributePaths = {"createdBy", "assignedTo"})
    List<Incident> findAllByOrderByCreatedAtDesc();

    long countByCreatedBy(User user);
    long countByCreatedByAndStatus(User user, Status status);
    long countByCreatedByAndStatusIn(User user, Collection<Status> statuses);

    long countByAssignedTo(User user);
    long countByAssignedToAndStatus(User user, Status status);
    long countByAssignedToAndStatusIn(User user, Collection<Status> statuses);
    long countByAssignedToAndPriority(User user, Priority priority);

    long countByStatus(Status status);
}
