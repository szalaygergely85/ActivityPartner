package com.gege.activitypartner.repository;

import com.gege.activitypartner.entity.RefreshToken;
import com.gege.activitypartner.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUserAndIsActiveTrue(User user);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isActive = false WHERE rt.user.id = :userId")
    void deactivateAllUserTokens(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isActive = false WHERE rt.token = :token")
    void deactivateToken(@Param("token") String token);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    // Count active sessions for a user
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.isActive = true")
    long countActiveTokensByUserId(@Param("userId") Long userId);
}
