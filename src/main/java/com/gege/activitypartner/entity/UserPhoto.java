package com.gege.activitypartner.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_photos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 1000)
    private String photoUrl;

    @Column(nullable = false)
    private Boolean isProfilePicture = false; // Flag to indicate which photo is the main profile picture

    @Column(nullable = false)
    private Integer displayOrder = 0; // Order to display photos (1-6)

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;
}
