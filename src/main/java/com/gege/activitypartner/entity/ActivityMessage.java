package com.gege.activitypartner.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "activity_messages")
public class ActivityMessage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "activity_id", nullable = false)
  @ToString.Exclude
  private Activity activity;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  @ToString.Exclude
  private User user;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String messageText;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private Boolean isDeleted = false;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
}
