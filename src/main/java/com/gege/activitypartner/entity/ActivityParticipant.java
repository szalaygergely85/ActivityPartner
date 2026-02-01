package com.gege.activitypartner.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
    name = "activity_participants",
    uniqueConstraints = @UniqueConstraint(columnNames = {"activity_id", "user_id"}),
    indexes = {
      @Index(name = "idx_participant_activity", columnList = "activity_id"),
      @Index(name = "idx_participant_user", columnList = "user_id"),
      @Index(name = "idx_participant_status", columnList = "status"),
      @Index(name = "idx_participant_user_status", columnList = "user_id, status")
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityParticipant {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "activity_id", nullable = false)
  @ToString.Exclude
  private Activity activity;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  @ToString.Exclude
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ParticipantStatus status = ParticipantStatus.INTERESTED;

  @Column(nullable = false)
  private Boolean isFriend = false;

  @Column(nullable = false)
  private Integer applicationAttempts = 1;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime joinedAt;

  @UpdateTimestamp private LocalDateTime updatedAt;
}
