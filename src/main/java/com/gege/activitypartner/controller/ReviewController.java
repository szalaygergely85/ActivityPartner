package com.gege.activitypartner.controller;

import com.gege.activitypartner.config.SecurityContextUtil;
import com.gege.activitypartner.dto.ReviewRequest;
import com.gege.activitypartner.dto.ReviewResponse;
import com.gege.activitypartner.dto.ReviewUpdateRequest;
import com.gege.activitypartner.entity.User;
import com.gege.activitypartner.repository.UserRepository;
import com.gege.activitypartner.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Configure properly in production
public class ReviewController {

    private final ReviewService reviewService;
    private final SecurityContextUtil securityContextUtil;
    private final UserRepository userRepository;

    // Create review
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody ReviewRequest request) {
        Long userId = securityContextUtil.getCurrentUserId();
        ReviewResponse response = reviewService.createReview(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get all reviews for a user (reviews received)
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsForUser(@PathVariable Long userId) {
        List<ReviewResponse> reviews = reviewService.getReviewsForUser(userId);
        return ResponseEntity.ok(reviews);
    }

    // Get reviews written by a reviewer
    @GetMapping("/reviewer/{reviewerId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByReviewer(@PathVariable Long reviewerId) {
        List<ReviewResponse> reviews = reviewService.getReviewsByReviewer(reviewerId);
        return ResponseEntity.ok(reviews);
    }

    // Get all reviews for an activity
    @GetMapping("/activity/{activityId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsForActivity(@PathVariable Long activityId) {
        List<ReviewResponse> reviews = reviewService.getReviewsForActivity(activityId);
        return ResponseEntity.ok(reviews);
    }

    // Update own review
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewUpdateRequest request) {
        Long userId = securityContextUtil.getCurrentUserId();
        ReviewResponse response = reviewService.updateReview(id, request, userId);
        return ResponseEntity.ok(response);
    }

    // Delete own review
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        Long userId = securityContextUtil.getCurrentUserId();
        reviewService.deleteReview(id, userId);
        return ResponseEntity.noContent().build();
    }
}
