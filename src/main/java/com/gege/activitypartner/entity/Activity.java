package com.gege.activitypartner.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "activities")
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

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActivityParticipant> participants = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Utility method to calculate available spots
    @Transient
    public Integer getAvailableSpots() {
        long joinedCount = participants.stream()
                .filter(p -> p.getStatus() == ParticipantStatus.JOINED)
                .count();
        return totalSpots - (int) joinedCount;
    }

    // Check if activity is full
    @Transient
    public boolean isFull() {
        return getAvailableSpots() <= 0;
    }
}
