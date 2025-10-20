package com.gege.activitypartner.service;

import com.gege.activitypartner.dto.ReviewRequest;
import com.gege.activitypartner.dto.ReviewResponse;
import com.gege.activitypartner.dto.ReviewUpdateRequest;
import com.gege.activitypartner.dto.UserSimpleResponse;
import com.gege.activitypartner.entity.*;
import com.gege.activitypartner.exception.DuplicateResourceException;
import com.gege.activitypartner.exception.InvalidParticipantActionException;
import com.gege.activitypartner.exception.ResourceNotFoundException;
import com.gege.activitypartner.repository.ActivityRepository;
import com.gege.activitypartner.repository.ReviewRepository;
import com.gege.activitypartner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final NotificationService notificationService;

    // Create review
    public ReviewResponse createReview(ReviewRequest request, Long reviewerId) {
        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found with id: " + reviewerId));

        User reviewedUser = userRepository.findById(request.getReviewedUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Reviewed user not found with id: " + request.getReviewedUserId()));

        Activity activity = activityRepository.findById(request.getActivityId())
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id: " + request.getActivityId()));

        // Validate business rules
        validateReviewCreation(reviewer, reviewedUser, activity);

        // Check for duplicate review
        if (reviewRepository.existsByReviewerIdAndReviewedUserIdAndActivityId(
                reviewerId, request.getReviewedUserId(), request.getActivityId())) {
            throw new DuplicateResourceException("You have already reviewed this user for this activity");
        }

        // Create review
        Review review = new Review();
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setActivityId(request.getActivityId());
        review.setReviewer(reviewer);
        review.setReviewedUser(reviewedUser);

        Review saved = reviewRepository.save(review);

        // Recalculate reviewed user's average rating
        updateUserRating(reviewedUser.getId());

        // Notify reviewed user about the new review
        notificationService.createAndSendNotification(
                reviewedUser,
                "New Review Received",
                reviewer.getFullName() + " left you a " + request.getRating() + "-star review",
                NotificationType.REVIEW_RECEIVED,
                request.getActivityId(),
                null,
                saved.getId()
        );

        return mapToResponse(saved);
    }

    // Get all reviews for a user (reviews received)
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsForUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return reviewRepository.findByReviewedUserIdWithReviewer(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Get reviews written by a reviewer
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByReviewer(Long reviewerId) {
        if (!userRepository.existsById(reviewerId)) {
            throw new ResourceNotFoundException("Reviewer not found with id: " + reviewerId);
        }

        return reviewRepository.findByReviewerIdWithReviewedUser(reviewerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Get all reviews for an activity
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsForActivity(Long activityId) {
        if (!activityRepository.existsById(activityId)) {
            throw new ResourceNotFoundException("Activity not found with id: " + activityId);
        }

        return reviewRepository.findByActivityIdWithDetails(activityId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Update own review
    public ReviewResponse updateReview(Long reviewId, ReviewUpdateRequest request, Long reviewerId) {
        Review review = reviewRepository.findByIdWithDetails(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        // Check if user is the reviewer
        if (!review.getReviewer().getId().equals(reviewerId)) {
            throw new InvalidParticipantActionException("You can only update your own reviews");
        }

        // Update fields
        if (request.getRating() != null) {
            review.setRating(request.getRating());
        }
        if (request.getComment() != null) {
            review.setComment(request.getComment());
        }

        Review updated = reviewRepository.save(review);

        // Recalculate reviewed user's average rating
        updateUserRating(review.getReviewedUser().getId());

        return mapToResponse(updated);
    }

    // Delete own review
    public void deleteReview(Long reviewId, Long reviewerId) {
        Review review = reviewRepository.findByIdWithDetails(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        // Check if user is the reviewer
        if (!review.getReviewer().getId().equals(reviewerId)) {
            throw new InvalidParticipantActionException("You can only delete your own reviews");
        }

        Long reviewedUserId = review.getReviewedUser().getId();
        reviewRepository.delete(review);

        // Recalculate reviewed user's average rating
        updateUserRating(reviewedUserId);
    }

    // Validation helpers
    private void validateReviewCreation(User reviewer, User reviewedUser, Activity activity) {
        // Cannot review yourself
        if (reviewer.getId().equals(reviewedUser.getId())) {
            throw new InvalidParticipantActionException("Cannot review yourself");
        }

        // Activity must be completed
        if (activity.getStatus() != ActivityStatus.COMPLETED) {
            throw new InvalidParticipantActionException("Can only review after activity is completed");
        }
    }

    // Update user's average rating
    private void updateUserRating(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Double averageRating = reviewRepository.calculateAverageRating(userId);

        // Set rating to 0.0 if no reviews exist
        user.setRating(averageRating != null ? averageRating : 0.0);
        userRepository.save(user);
    }

    // Mapping helper
    private ReviewResponse mapToResponse(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setActivityId(review.getActivityId());

        UserSimpleResponse reviewerResponse = new UserSimpleResponse();
        reviewerResponse.setId(review.getReviewer().getId());
        reviewerResponse.setFullName(review.getReviewer().getFullName());
        reviewerResponse.setProfileImageUrl(review.getReviewer().getProfileImageUrl());
        reviewerResponse.setRating(review.getReviewer().getRating());
        reviewerResponse.setBadge(review.getReviewer().getBadge());
        response.setReviewer(reviewerResponse);

        UserSimpleResponse reviewedUserResponse = new UserSimpleResponse();
        reviewedUserResponse.setId(review.getReviewedUser().getId());
        reviewedUserResponse.setFullName(review.getReviewedUser().getFullName());
        reviewedUserResponse.setProfileImageUrl(review.getReviewedUser().getProfileImageUrl());
        reviewedUserResponse.setRating(review.getReviewedUser().getRating());
        reviewedUserResponse.setBadge(review.getReviewedUser().getBadge());
        response.setReviewedUser(reviewedUserResponse);

        response.setCreatedAt(review.getCreatedAt());

        return response;
    }
}
