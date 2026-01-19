package com.gege.activitypartner.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Entity
@Table(name = "account_deletion_requests")
public class AccountDeletionRequest {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private LocalDateTime requestedAt;

  @Column private LocalDateTime processedAt;

  @PrePersist
  protected void onCreate() {
    requestedAt = LocalDateTime.now();
  }
}
