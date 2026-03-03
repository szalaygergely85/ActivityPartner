package com.gege.activitypartner.repository;

import com.gege.activitypartner.entity.PasswordResetToken;
import com.gege.activitypartner.entity.User;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

  Optional<PasswordResetToken> findByToken(String token);

  void deleteByUser(User user);

  @Modifying
  @Query("DELETE FROM PasswordResetToken t WHERE t.expiryDate < :now")
  void deleteExpiredTokens(@Param("now") LocalDateTime now);
}
