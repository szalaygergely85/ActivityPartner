package com.gege.activitypartner.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String name;

  @Column(length = 200)
  private String description;

  @Column(length = 10)
  private String icon;

  @Column(length = 100)
  private String imageResourceName;

  @Column(nullable = false)
  private Boolean isActive = true;

  @Column(nullable = false)
  private Integer activityCount = 0;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;
}
