package com.gege.activitypartner.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
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

    @Column(length = 500)
    private String bio;

    private String profileImageUrl;

    @Column(nullable = false)
    private Double rating = 0.0; // Average rating from reviews

    @Column(nullable = false)
    private Integer completedActivities = 0;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_interests", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "interest")
    private List<String> interests = new ArrayList<>(); // Activity types: Hiking, Coffee, Sports, etc.

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

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
