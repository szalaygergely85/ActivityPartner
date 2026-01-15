package com.gege.activitypartner.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "reports")
public class Report {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "reporter_id", nullable = false)
  @ToString.Exclude
  private User reporter;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ReportType reportType;

  @ManyToOne
  @JoinColumn(name = "reported_activity_id")
  @ToString.Exclude
  private Activity reportedActivity;

  @ManyToOne
  @JoinColumn(name = "reported_message_id")
  @ToString.Exclude
  private ActivityMessage reportedMessage;

  @ManyToOne
  @JoinColumn(name = "reported_user_id")
  @ToString.Exclude
  private User reportedUser;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String reason;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ReportStatus status = ReportStatus.PENDING;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Column private LocalDateTime resolvedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
}
