package com.backend.amealia.modules.user.entity;

import com.backend.amealia.audit.AuditableBase;
import com.backend.amealia.modules.user.enums.VerificationStatus;
import com.backend.amealia.modules.user.enums.VerificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RefreshToken extends AuditableBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(unique = true, nullable = false)
    private String hashedToken;

    @Column(unique = true, nullable = false)
    private String jti;

    private boolean revoked;

    private Instant expiresAt;
}
