package com.sdia.incidents.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Refresh token opaque (UUID aleatoire) stocke en base.
 * Le stockage permet de le revoquer lors de la deconnexion.
 */
@Entity
@Table(name = "refresh_tokens")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Instant expiryDate;

    @Column(nullable = false)
    private boolean revoked;
}
