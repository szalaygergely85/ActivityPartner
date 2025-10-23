package com.gege.activitypartner.service;

import com.gege.activitypartner.dto.*;
import com.gege.activitypartner.entity.*;
import com.gege.activitypartner.exception.DuplicateResourceException;
import com.gege.activitypartner.exception.InvalidParticipantActionException;
import com.gege.activitypartner.exception.ResourceNotFoundException;
import com.gege.activitypartner.repository.ActivityParticipantRepository;
import com.gege.activitypartner.repository.ActivityRepository;
import com.gege.activitypartner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ActivityParticipantService {

    private static final int MAX_APPLICATION_ATTEMPTS = 3;

    private final ActivityParticipantRepository participantRepository;
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    // Express interest in an activity
    public ParticipantResponse expressInterest(Long activityId, Long userId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id: " + activityId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Validate business rules
        validateInterestRequest(activity, user);

        // Check if already participating
        var existingParticipant = participantRepository.findByActivityIdAndUserId(activityId, userId);

        if (existingParticipant.isPresent()) {
            ActivityParticipant participant = existingParticipant.get();
            ParticipantStatus status = participant.getStatus();

            // If active status (INTERESTED, ACCEPTED, JOINED), cannot reapply
            if (status == ParticipantStatus.INTERESTED ||
                status == ParticipantStatus.ACCEPTED ||
                status == ParticipantStatus.JOINED) {
                throw new DuplicateResourceException("You have already expressed interest in this activity");
            }

            // If DECLINED, cannot reapply to prevent spam
            if (status == ParticipantStatus.DECLINED) {
                throw new InvalidParticipantActionException("Your application was declined. You cannot reapply to this activity.");
            }

            // If WITHDRAWN or LEFT, can reapply but check attempt limit first
            Long totalAttempts = participantRepository.countApplicationAttempts(activityId, userId);
            if (totalAttempts >= MAX_APPLICATION_ATTEMPTS) {
                throw new InvalidParticipantActionException(
                        "You have reached the maximum number of application attempts (" + MAX_APPLICATION_ATTEMPTS + ") for this activity"
                );
            }

            // Reuse existing record by updating status back to INTERESTED
            participant.setStatus(ParticipantStatus.INTERESTED);
            participant.setApplicationAttempts(totalAttempts.intValue() + 1);

            ActivityParticipant saved = participantRepository.save(participant);

            // Notify activity creator about renewed interest
            notificationService.createAndSendNotification(
                    activity.getCreator(),
                    "New Interest in Your Activity",
                    user.getFullName() + " is interested in \"" + activity.getTitle() + "\"",
                    NotificationType.PARTICIPANT_INTERESTED,
                    activity.getId(),
                    saved.getId(),
                    null
            );

            return mapToParticipantResponse(saved);
        }

        // No existing record - check application attempt limit
        Long totalAttempts = participantRepository.countApplicationAttempts(activityId, userId);
        if (totalAttempts >= MAX_APPLICATION_ATTEMPTS) {
            throw new InvalidParticipantActionException(
                    "You have reached the maximum number of application attempts (" + MAX_APPLICATION_ATTEMPTS + ") for this activity"
            );
        }

        // Create new participant record
        ActivityParticipant participant = new ActivityParticipant();
        participant.setActivity(activity);
        participant.setUser(user);
        participant.setStatus(ParticipantStatus.INTERESTED);
        participant.setIsFriend(false); // TODO: Implement friend check logic
        participant.setApplicationAttempts(totalAttempts.intValue() + 1);

        ActivityParticipant saved = participantRepository.save(participant);

        // Notify activity creator about new interest
        notificationService.createAndSendNotification(
                activity.getCreator(),
                "New Interest in Your Activity",
                user.getFullName() + " is interested in \"" + activity.getTitle() + "\"",
                NotificationType.PARTICIPANT_INTERESTED,
                activity.getId(),
                saved.getId(),
                null
        );

        return mapToParticipantResponse(saved);
    }

    // Get all participants for an activity
    @Transactional(readOnly = true)
    public List<ParticipantResponse> getActivityParticipants(Long activityId) {
        if (!activityRepository.existsById(activityId)) {
            throw new ResourceNotFoundException("Activity not found with id: " + activityId);
        }

        return participantRepository.findByActivityId(activityId).stream()
                .map(this::mapToParticipantResponse)
                .collect(Collectors.toList());
    }

    // Get interested users for an activity (creator only)
    @Transactional(readOnly = true)
    public List<ParticipantResponse> getInterestedUsers(Long activityId, Long creatorId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id: " + activityId));

        // Verify user is the creator
        if (!activity.getCreator().getId().equals(creatorId)) {
            throw new InvalidParticipantActionException("Only the activity creator can view interested users");
        }

        return participantRepository.findInterestedUsersByActivityId(activityId).stream()
                .map(this::mapToParticipantResponse)
                .collect(Collectors.toList());
    }

    // Get user's participated activities
    @Transactional(readOnly = true)
    public List<ParticipantActivityResponse> getMyParticipations(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return participantRepository.findByUserId(userId).stream()
                .map(this::mapToParticipantActivityResponse)
                .collect(Collectors.toList());
    }

    // Get user's participations filtered by status
    @Transactional(readOnly = true)
    public List<ParticipantActivityResponse> getMyParticipationsByStatus(Long userId, ParticipantStatus status) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return participantRepository.findByUserIdAndStatus(userId, status).stream()
                .map(this::mapToParticipantActivityResponse)
                .collect(Collectors.toList());
    }

    // Accept or decline participant (creator only)
    public ParticipantResponse updateParticipantStatus(Long participantId, ParticipantStatus newStatus, Long creatorId) {
        ActivityParticipant participant = participantRepository.findByIdWithDetails(participantId)
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found with id: " + participantId));

        // Verify user is the creator
        if (!participant.getActivity().getCreator().getId().equals(creatorId)) {
            throw new InvalidParticipantActionException("Only the activity creator can update participant status");
        }

        // Validate status transition
        if (participant.getStatus() != ParticipantStatus.INTERESTED) {
            throw new InvalidParticipantActionException("Can only accept/decline users with INTERESTED status");
        }

        if (newStatus != ParticipantStatus.ACCEPTED && newStatus != ParticipantStatus.DECLINED) {
            throw new InvalidParticipantActionException("Creator can only ACCEPT or DECLINE interested users");
        }

        // Check available spots for ACCEPTED
        if (newStatus == ParticipantStatus.ACCEPTED) {
            if (participant.getActivity().getAvailableSpots() <= 0) {
                throw new InvalidParticipantActionException("No available spots in this activity");
            }
        }

        participant.setStatus(newStatus);
        ActivityParticipant updated = participantRepository.save(participant);

        // Notify participant about status change
        String notificationTitle = newStatus == ParticipantStatus.ACCEPTED
                ? "Interest Accepted!"
                : "Interest Declined";
        String notificationMessage = newStatus == ParticipantStatus.ACCEPTED
                ? "Your interest in \"" + participant.getActivity().getTitle() + "\" was accepted. Confirm your participation!"
                : "Your interest in \"" + participant.getActivity().getTitle() + "\" was declined.";
        NotificationType notificationType = newStatus == ParticipantStatus.ACCEPTED
                ? NotificationType.PARTICIPANT_ACCEPTED
                : NotificationType.PARTICIPANT_DECLINED;

        notificationService.createAndSendNotification(
                participant.getUser(),
                notificationTitle,
                notificationMessage,
                notificationType,
                participant.getActivity().getId(),
                updated.getId(),
                null
        );

        return mapToParticipantResponse(updated);
    }

    // Confirm joining after acceptance (user confirms)
    public ParticipantResponse confirmJoining(Long participantId, Long userId) {
        ActivityParticipant participant = participantRepository.findByIdWithDetails(participantId)
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found with id: " + participantId));

        // Verify user is the participant
        if (!participant.getUser().getId().equals(userId)) {
            throw new InvalidParticipantActionException("You can only confirm your own participation");
        }

        // Validate current status
        if (participant.getStatus() != ParticipantStatus.ACCEPTED) {
            throw new InvalidParticipantActionException("Can only confirm participation after being accepted");
        }

        // Check activity is still open
        if (participant.getActivity().getStatus() != ActivityStatus.OPEN) {
            throw new InvalidParticipantActionException("Cannot join - activity is not open");
        }

        // Check available spots
        if (participant.getActivity().getAvailableSpots() <= 0) {
            throw new InvalidParticipantActionException("No available spots in this activity");
        }

        participant.setStatus(ParticipantStatus.JOINED);
        ActivityParticipant updated = participantRepository.save(participant);

        // Notify activity creator that participant joined
        notificationService.createAndSendNotification(
                participant.getActivity().getCreator(),
                "Participant Confirmed!",
                participant.getUser().getFullName() + " has joined \"" + participant.getActivity().getTitle() + "\"",
                NotificationType.PARTICIPANT_JOINED,
                participant.getActivity().getId(),
                updated.getId(),
                null
        );

        return mapToParticipantResponse(updated);
    }

    // Leave activity
    public void leaveActivity(Long activityId, Long userId) {
        ActivityParticipant participant = participantRepository.findByActivityIdAndUserId(activityId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Participation not found"));

        // Handle based on current status
        if (participant.getStatus() == ParticipantStatus.DECLINED) {
            throw new InvalidParticipantActionException("Cannot withdraw a declined application");
        }

        if (participant.getStatus() == ParticipantStatus.LEFT ||
            participant.getStatus() == ParticipantStatus.WITHDRAWN) {
            throw new InvalidParticipantActionException("You have already left this activity");
        }

        // For INTERESTED or ACCEPTED, mark as WITHDRAWN
        if (participant.getStatus() == ParticipantStatus.INTERESTED ||
            participant.getStatus() == ParticipantStatus.ACCEPTED) {
            participant.setStatus(ParticipantStatus.WITHDRAWN);
            participantRepository.save(participant);
            return;
        }

        // For JOINED, mark as LEFT and notify creator
        if (participant.getStatus() == ParticipantStatus.JOINED) {
            participant.setStatus(ParticipantStatus.LEFT);
            participantRepository.save(participant);

            // Notify activity creator that participant left
            notificationService.createAndSendNotification(
                    participant.getActivity().getCreator(),
                    "Participant Left",
                    participant.getUser().getFullName() + " has left \"" + participant.getActivity().getTitle() + "\"",
                    NotificationType.PARTICIPANT_LEFT,
                    participant.getActivity().getId(),
                    participant.getId(),
                    null
            );
            return;
        }

        throw new InvalidParticipantActionException("Invalid status for leaving activity");
    }

    // Delete interest before acceptance (DEPRECATED - use leaveActivity instead)
    public void deleteInterest(Long activityId, Long userId) {
        ActivityParticipant participant = participantRepository.findByActivityIdAndUserId(activityId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Interest not found"));

        // Can only delete if WITHDRAWN (after user withdrew interest)
        // DECLINED applications cannot be deleted to preserve attempt history
        if (participant.getStatus() != ParticipantStatus.WITHDRAWN) {
            throw new InvalidParticipantActionException("Can only delete after withdrawing interest. Use leaveActivity to withdraw.");
        }

        participantRepository.delete(participant);
    }

    // Validation helpers
    private void validateInterestRequest(Activity activity, User user) {
        // Cannot join own activity
        if (activity.getCreator().getId().equals(user.getId())) {
            throw new InvalidParticipantActionException("Cannot join your own activity");
        }

        // Activity must be OPEN
        if (activity.getStatus() != ActivityStatus.OPEN) {
            throw new InvalidParticipantActionException("Cannot join - activity is not open");
        }

        // Check if activity is full (just a warning, can still express interest)
        if (activity.isFull()) {
            // Allow expressing interest even if full - creator might increase spots
            // or someone might leave
        }
    }

    // Mapping helpers
    private ParticipantResponse mapToParticipantResponse(ActivityParticipant participant) {
        ParticipantResponse response = new ParticipantResponse();
        response.setId(participant.getId());
        response.setActivityId(participant.getActivity().getId());
        response.setActivityTitle(participant.getActivity().getTitle());

        UserSimpleResponse userResponse = new UserSimpleResponse();
        userResponse.setId(participant.getUser().getId());
        userResponse.setFullName(participant.getUser().getFullName());
        userResponse.setProfileImageUrl(participant.getUser().getProfileImageUrl());
        userResponse.setRating(participant.getUser().getRating());
        userResponse.setBadge(participant.getUser().getBadge());
        response.setUser(userResponse);

        response.setStatus(participant.getStatus());
        response.setIsFriend(participant.getIsFriend());
        response.setJoinedAt(participant.getJoinedAt());
        response.setUpdatedAt(participant.getUpdatedAt());

        return response;
    }

    private ParticipantActivityResponse mapToParticipantActivityResponse(ActivityParticipant participant) {
        ParticipantActivityResponse response = new ParticipantActivityResponse();
        response.setParticipantId(participant.getId());
        response.setActivity(mapActivityToDTO(participant.getActivity()));
        response.setStatus(participant.getStatus());
        response.setIsFriend(participant.getIsFriend());
        response.setJoinedAt(participant.getJoinedAt());
        response.setUpdatedAt(participant.getUpdatedAt());

        return response;
    }

    private ActivityResponseDTO mapActivityToDTO(Activity activity) {
        ActivityResponseDTO dto = new ActivityResponseDTO();
        dto.setId(activity.getId());
        dto.setTitle(activity.getTitle());
        dto.setDescription(activity.getDescription());
        dto.setActivityDate(activity.getActivityDate());
        dto.setLocation(activity.getLocation());
        dto.setCategory(activity.getCategory());
        dto.setTotalSpots(activity.getTotalSpots());
        dto.setAvailableSpots(activity.getAvailableSpots());
        dto.setReservedForFriendsSpots(activity.getReservedForFriendsSpots());
        dto.setMinParticipants(activity.getMinParticipants());
        dto.setStatus(activity.getStatus());
        dto.setTrending(activity.getTrending());
        dto.setDifficulty(activity.getDifficulty());
        dto.setCost(activity.getCost());
        dto.setMinAge(activity.getMinAge());
        dto.setCreatorId(activity.getCreator().getId());
        dto.setCreatorName(activity.getCreator().getFullName());
        dto.setCreatorImageUrl(activity.getCreator().getProfileImageUrl());
        dto.setParticipantsCount(activity.getParticipants().size());
        dto.setCreatedAt(activity.getCreatedAt());
        dto.setUpdatedAt(activity.getUpdatedAt());
        return dto;
    }
}
