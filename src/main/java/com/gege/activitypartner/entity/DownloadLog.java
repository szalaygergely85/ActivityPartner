package com.gege.activitypartner.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Entity
@Table(name = "download_logs")
public class DownloadLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(length = 100)
  private String appVersion;

  @Column(length = 100)
  private String osVersion;

  @Column(length = 100)
  private String deviceModel;

  @Column(length = 50)
  private String platform; // android, ios

  @Column(length = 100)
  private String country;

  @Column(length = 100)
  private String language;

  @Column(nullable = false)
  private LocalDateTime downloadedAt;

  @PrePersist
  protected void onCreate() {
    downloadedAt = LocalDateTime.now();
  }
}
