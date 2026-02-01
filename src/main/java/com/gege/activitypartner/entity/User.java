package com.gege.activitypartner.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
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
    name = "users",
    indexes = {
      @Index(name = "idx_user_email", columnList = "email"),
      @Index(name = "idx_user_active", columnList = "is_active"),
      @Index(name = "idx_user_email_active", columnList = "email, is_active")
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String fullName;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String password; // Will be encrypted with BCrypt

  @Column(nullable = false)
  private LocalDate birthDate;

  @Column(length = 500)
  private String bio;

  @Column(length = 1000)
  private String profileImageUrl;

  @Column(length = 100)
  private String city; // User's city name (for autocomplete search)

  @Column(length = 255)
  private String placeId; // Google Places ID for the city

  @Column(precision = 10, scale = 8)
  private BigDecimal latitude; // User's city latitude for distance calculation

  @Column(precision = 11, scale = 8)
  private BigDecimal longitude; // User's city longitude for distance calculation

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  @ToString.Exclude
  private List<UserPhoto> photos = new ArrayList<>(); // All user photos (up to 6)

  @Column(nullable = false)
  private Double rating = 0.0; // Average rating from reviews

  @Column(nullable = false)
  private Integer completedActivities = 0;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "user_interests", joinColumns = @JoinColumn(name = "user_id"))
  @Column(name = "interest")
  private List<String> interests =
      new ArrayList<>(); // Activity types: Hiking, Coffee, Sports, etc.

  private String badge; // Special badges: ⭐, 👑, 🏔️, 💎

  @Column(length = 500)
  private String fcmToken; // Firebase Cloud Messaging device token for push notifications

  @Column(nullable = false)
  private Boolean notificationsEnabled = true; // User preference for receiving notifications

  @Column(nullable = false)
  private Boolean isActive = true;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp private LocalDateTime updatedAt;
}
