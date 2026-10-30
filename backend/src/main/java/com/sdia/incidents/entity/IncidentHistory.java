package com.sdia.incidents.entity;

import com.sdia.incidents.enums.Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Trace d'un changement important sur un incident :
 * creation, affectation, changement de statut.
 */
@Entity
@Table(name = "incident_history")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class IncidentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    /** CREATED, ASSIGNED, STATUS_CHANGED */
    @Column(nullable = false, length = 30)
    private String action;

    @Column(nullable = false, length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Status oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Status newStatus;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "performed_by_id", nullable = false)
    private User performedBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
