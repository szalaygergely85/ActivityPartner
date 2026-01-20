package com.gege.activitypartner.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Entity
@Table(name = "app_logs")
public class AppLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(length = 20)
  private String level; // INFO, WARN, ERROR, DEBUG

  @Column(length = 100)
  private String tag; // e.g., "AUTH", "NETWORK", "UI", "PAYMENT"

  @Column(columnDefinition = "TEXT")
  private String message;

  @Column(length = 100)
  private String appVersion;

  @Column(length = 100)
  private String osVersion;

  @Column(length = 100)
  private String deviceModel;

  @Column(length = 50)
  private String platform;

  @Column
  private Long userId;

  @Column(nullable = false)
  private LocalDateTime loggedAt;

  @Column(nullable = false)
  private LocalDateTime receivedAt;

  @PrePersist
  protected void onCreate() {
    receivedAt = LocalDateTime.now();
  }
}
