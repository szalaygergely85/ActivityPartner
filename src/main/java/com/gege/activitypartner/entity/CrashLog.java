package com.gege.activitypartner.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Entity
@Table(name = "crash_logs")
public class CrashLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(length = 100)
  private String appVersion;

  @Column(length = 100)
  private String osVersion;

  @Column(length = 100)
  private String deviceModel;

  @Column(columnDefinition = "TEXT")
  private String errorMessage;

  @Column(columnDefinition = "LONGTEXT")
  private String stackTrace;

  @Column private Long userId;

  @Column(length = 50)
  private String platform; // android, ios

  @Column(nullable = false)
  private LocalDateTime crashedAt;

  @Column(nullable = false)
  private LocalDateTime receivedAt;

  @PrePersist
  protected void onCreate() {
    receivedAt = LocalDateTime.now();
  }
}
