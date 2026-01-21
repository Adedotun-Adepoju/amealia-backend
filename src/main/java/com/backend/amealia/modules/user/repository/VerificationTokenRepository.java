package com.backend.amealia.modules.user.repository;

import com.backend.amealia.modules.user.entity.User;
import com.backend.amealia.modules.user.entity.VerificationToken;
import com.backend.amealia.modules.user.enums.VerificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    @Modifying
    @Transactional
    @Query("""
        UPDATE VerificationToken t
        SET t.active =false
        WHERE t.user.id = :userId AND t.active = true AND t.type = :verificationType
    """)
    int deactivateTokens(@Param("userId") Long userId, @Param("verificationType") VerificationType verificationType);
}
