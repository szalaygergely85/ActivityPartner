package com.gege.activitypartner.repository;

import com.gege.activitypartner.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Find all reviews for a user (reviews received)
    List<Review> findByReviewedUserId(Long reviewedUserId);

    // Find all reviews written by a reviewer
    List<Review> findByReviewerId(Long reviewerId);

    // Find all reviews for an activity
    List<Review> findByActivityId(Long activityId);

    // Check if review exists
    boolean existsByReviewerIdAndReviewedUserIdAndActivityId(Long reviewerId, Long reviewedUserId, Long activityId);

    // Find specific review
    Optional<Review> findByReviewerIdAndReviewedUserIdAndActivityId(Long reviewerId, Long reviewedUserId, Long activityId);

    // Calculate average rating for a user
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.reviewedUser.id = :userId")
    Double calculateAverageRating(@Param("userId") Long userId);

    // Count total reviews for a user
    @Query("SELECT COUNT(r) FROM Review r WHERE r.reviewedUser.id = :userId")
    Long countReviewsForUser(@Param("userId") Long userId);

    // Get reviews with reviewer details
    @Query("SELECT r FROM Review r " +
           "JOIN FETCH r.reviewer " +
           "WHERE r.reviewedUser.id = :userId " +
           "ORDER BY r.createdAt DESC")
    List<Review> findByReviewedUserIdWithReviewer(@Param("userId") Long userId);

    // Get reviews with reviewed user details
    @Query("SELECT r FROM Review r " +
           "JOIN FETCH r.reviewedUser " +
           "WHERE r.reviewer.id = :reviewerId " +
           "ORDER BY r.createdAt DESC")
    List<Review> findByReviewerIdWithReviewedUser(@Param("reviewerId") Long reviewerId);

    // Get reviews for activity with all user details
    @Query("SELECT r FROM Review r " +
           "JOIN FETCH r.reviewer " +
           "JOIN FETCH r.reviewedUser " +
           "WHERE r.activityId = :activityId " +
           "ORDER BY r.createdAt DESC")
    List<Review> findByActivityIdWithDetails(@Param("activityId") Long activityId);

    // Find review by id with details
    @Query("SELECT r FROM Review r " +
           "JOIN FETCH r.reviewer " +
           "JOIN FETCH r.reviewedUser " +
           "WHERE r.id = :id")
    Optional<Review> findByIdWithDetails(@Param("id") Long id);
}
