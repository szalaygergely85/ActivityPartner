package com.gege.activitypartner.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(
    name = "reviews",
    uniqueConstraints =
        @UniqueConstraint(columnNames = {"reviewer_id", "reviewed_user_id", "activity_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Min(value = 1, message = "Rating must be at least 1")
  @Max(value = 5, message = "Rating must be at most 5")
  @Column(nullable = false)
  private Integer rating;

  @Column(length = 500)
  private String comment;

  @Column(nullable = false)
  private Long activityId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reviewer_id", nullable = false)
  @ToString.Exclude
  private User reviewer;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reviewed_user_id", nullable = false)
  @ToString.Exclude
  private User reviewedUser;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;
}
