package com.gege.activitypartner.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
    name = "activities",
    indexes = {
      @Index(name = "idx_activity_status", columnList = "status"),
      @Index(name = "idx_activity_date", columnList = "activityDate"),
      @Index(name = "idx_activity_status_date", columnList = "status, activityDate"),
      @Index(name = "idx_activity_creator", columnList = "creator_id"),
      @Index(name = "idx_activity_category", columnList = "category"),
      @Index(name = "idx_activity_location", columnList = "latitude, longitude"),
      @Index(name = "idx_activity_trending", columnList = "trending")
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Activity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(length = 1000)
  private String description;

  @Column(nullable = false)
  private LocalDateTime activityDate;

  @Column(nullable = false)
  private String location; // Location name

  @Column(length = 255)
  private String placeId; // Google Places ID for the location

  @Column(precision = 10, scale = 8)
  private BigDecimal latitude; // Geographic latitude

  @Column(precision = 11, scale = 8)
  private BigDecimal longitude; // Geographic longitude

  @Column(nullable = false)
  private String category; // "Hiking", "Coffee", "Sports", etc.

  // Spots Management
  @Column(nullable = false)
  private Integer totalSpots;

  private Integer reservedForFriendsSpots = 0;

  private Integer minParticipants; // Optional: minimum needed for activity to happen

  // Status & Metadata
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ActivityStatus status = ActivityStatus.OPEN;

  @Column(nullable = false)
  private Boolean trending = false;

  // Additional Info
  private String difficulty; // "Easy", "Moderate", "Hard"

  @Column(nullable = false)
  private Double cost = 0.0; // 0.0 = free

  private Integer minAge; // Age restriction if needed

  @Column(length = 255)
  private String coverImageUrl; // Optional user-selected cover image URL

  @Column(nullable = false)
  private Boolean reminderSent = false; // Track if reminder notification was sent

  // Relationships
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "creator_id", nullable = false)
  private User creator;

  @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
  @ToString.Exclude
  private List<ActivityParticipant> participants = new ArrayList<>();

  // Activity Interests/Tags (multiple interests can be added to an activity)
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "activity_interests", joinColumns = @JoinColumn(name = "activity_id"))
  @Column(name = "interest")
  private List<String> interests = new ArrayList<>(); // e.g., ["outdoor", "nature", "exercise"]

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp private LocalDateTime updatedAt;

  // Utility method to calculate available spots
  @Transient
  public Integer getAvailableSpots() {
    long reservedCount =
        participants.stream()
            .filter(
                p ->
                    p.getStatus() == ParticipantStatus.ACCEPTED
                        || p.getStatus() == ParticipantStatus.JOINED)
            .count();
    return totalSpots - (int) reservedCount;
  }

  // Check if activity is full
  @Transient
  public boolean isFull() {
    return getAvailableSpots() <= 0;
  }
}
