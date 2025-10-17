package com.gege.activitypartner.controller;

import com.gege.activitypartner.dto.ReviewRequest;
import com.gege.activitypartner.dto.ReviewResponse;
import com.gege.activitypartner.dto.ReviewUpdateRequest;
import com.gege.activitypartner.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Configure properly in production
public class ReviewController {

    private final ReviewService reviewService;

    // Create review
    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @Valid @RequestBody ReviewRequest request,
            @RequestParam Long reviewerId) { // TODO: Replace with authenticated user from JWT
        ReviewResponse response = reviewService.createReview(request, reviewerId);
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
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewUpdateRequest request,
            @RequestParam Long reviewerId) { // TODO: Replace with authenticated user from JWT
        ReviewResponse response = reviewService.updateReview(id, request, reviewerId);
        return ResponseEntity.ok(response);
    }

    // Delete own review
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long id,
            @RequestParam Long reviewerId) { // TODO: Replace with authenticated user from JWT
        reviewService.deleteReview(id, reviewerId);
        return ResponseEntity.noContent().build();
    }
}
