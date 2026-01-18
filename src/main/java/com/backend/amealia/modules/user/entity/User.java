package com.backend.amealia.modules.user.entity;

import com.backend.amealia.audit.AuditableBase;
import com.backend.amealia.modules.user.enums.OnboardingStep;
import com.backend.amealia.modules.user.enums.UserStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Collection;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User extends AuditableBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(unique = true)
    private String phoneNumber;

    private boolean emailVerified;
    private boolean phoneVerified;

    @Enumerated(EnumType.STRING)
    private OnboardingStep onboardingStep;

    @Enumerated(EnumType.STRING)
    private UserStatus userStatus;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    private Collection<VerificationToken> verificationTokens;
}
