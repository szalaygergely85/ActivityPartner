package com.gege.activitypartner.service;

import com.gege.activitypartner.dto.ActivityMessageRequest;
import com.gege.activitypartner.dto.ActivityMessageResponse;
import com.gege.activitypartner.entity.*;
import com.gege.activitypartner.exception.InvalidParticipantActionException;
import com.gege.activitypartner.exception.ResourceNotFoundException;
import com.gege.activitypartner.repository.ActivityMessageRepository;
import com.gege.activitypartner.repository.ActivityParticipantRepository;
import com.gege.activitypartner.repository.ActivityRepository;
import com.gege.activitypartner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityMessageService {

    private final ActivityMessageRepository messageRepository;
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final ActivityParticipantRepository participantRepository;
    private final NotificationService notificationService;

    /**
     * Send a message in activity chat
     * Only accepted/joined participants can send messages
     */
    @Transactional
    public ActivityMessageResponse sendMessage(Long activityId, Long userId, ActivityMessageRequest request) {
        // Validate activity exists
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id: " + activityId));

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Check if user is the creator OR an accepted/joined participant
        boolean isCreator = activity.getCreator().getId().equals(userId);
        boolean isParticipant = participantRepository.findByActivityIdAndUserId(activityId, userId)
                .map(p -> p.getStatus() == ParticipantStatus.ACCEPTED || p.getStatus() == ParticipantStatus.JOINED)
                .orElse(false);

        if (!isCreator && !isParticipant) {
            throw new InvalidParticipantActionException("Only accepted participants can send messages");
        }

        // Validate message text
        if (request.getMessageText() == null || request.getMessageText().trim().isEmpty()) {
            throw new IllegalArgumentException("Message text cannot be empty");
        }

        // Create and save message
        ActivityMessage message = new ActivityMessage();
        message.setActivity(activity);
        message.setUser(user);
        message.setMessageText(request.getMessageText().trim());

        ActivityMessage saved = messageRepository.save(message);

        // Send notifications to all participants (except the sender)
        sendMessageNotifications(activity, user, saved);

        return mapToResponse(saved, userId);
    }

    /**
     * Get all messages for an activity
     * Only creator and accepted/joined participants can view messages
     */
    @Transactional(readOnly = true)
    public List<ActivityMessageResponse> getActivityMessages(Long activityId, Long userId) {
        // Validate activity exists
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id: " + activityId));

        // Check if user has access
        validateUserAccess(activity, userId);

        return messageRepository.findByActivityIdAndNotDeleted(activityId).stream()
                .map(message -> mapToResponse(message, userId))
                .collect(Collectors.toList());
    }

    /**
     * Get messages since a specific timestamp (for polling)
     */
    @Transactional(readOnly = true)
    public List<ActivityMessageResponse> getMessagesSince(Long activityId, Long userId, LocalDateTime since) {
        // Validate activity exists
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id: " + activityId));

        // Check if user has access
        validateUserAccess(activity, userId);

        return messageRepository.findByActivityIdSince(activityId, since).stream()
                .map(message -> mapToResponse(message, userId))
                .collect(Collectors.toList());
    }

    /**
     * Get message count for an activity
     */
    @Transactional(readOnly = true)
    public Long getMessageCount(Long activityId, Long userId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id: " + activityId));

        validateUserAccess(activity, userId);

        return messageRepository.countByActivityId(activityId);
    }

    /**
     * Delete a message (soft delete)
     * Only the message sender or activity creator can delete
     */
    @Transactional
    public void deleteMessage(Long messageId, Long userId) {
        ActivityMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + messageId));

        // Check if user is the sender or the activity creator
        boolean isSender = message.getUser().getId().equals(userId);
        boolean isCreator = message.getActivity().getCreator().getId().equals(userId);

        if (!isSender && !isCreator) {
            throw new InvalidParticipantActionException("Only the message sender or activity creator can delete messages");
        }

        // Soft delete
        message.setIsDeleted(true);
        messageRepository.save(message);
    }

    /**
     * Validate user has access to view messages
     */
    private void validateUserAccess(Activity activity, Long userId) {
        boolean isCreator = activity.getCreator().getId().equals(userId);
        boolean isParticipant = participantRepository.findByActivityIdAndUserId(activity.getId(), userId)
                .map(p -> p.getStatus() == ParticipantStatus.ACCEPTED || p.getStatus() == ParticipantStatus.JOINED)
                .orElse(false);

        if (!isCreator && !isParticipant) {
            throw new InvalidParticipantActionException("Only accepted participants can view messages");
        }
    }

    /**
     * Send notifications to all participants about new message
     */
    private void sendMessageNotifications(Activity activity, User sender, ActivityMessage message) {
        // Get all accepted/joined participants (excluding the sender)
        List<ActivityParticipant> participants = participantRepository.findByActivityId(activity.getId())
                .stream()
                .filter(p -> (p.getStatus() == ParticipantStatus.ACCEPTED || p.getStatus() == ParticipantStatus.JOINED)
                        && !p.getUser().getId().equals(sender.getId()))
                .collect(Collectors.toList());

        // Also notify creator if they're not the sender
        boolean shouldNotifyCreator = !activity.getCreator().getId().equals(sender.getId());

        // Notify participants
        for (ActivityParticipant participant : participants) {
            notificationService.createAndSendNotification(
                    participant.getUser(),
                    "New message in " + activity.getTitle(),
                    sender.getFullName() + ": " + truncateMessage(message.getMessageText()),
                    NotificationType.NEW_MESSAGE,
                    activity.getId(),
                    null,
                    null
            );
        }

        // Notify creator if needed
        if (shouldNotifyCreator) {
            notificationService.createAndSendNotification(
                    activity.getCreator(),
                    "New message in " + activity.getTitle(),
                    sender.getFullName() + ": " + truncateMessage(message.getMessageText()),
                    NotificationType.NEW_MESSAGE,
                    activity.getId(),
                    null,
                    null
            );
        }
    }

    /**
     * Truncate message for notification preview
     */
    private String truncateMessage(String message) {
        if (message.length() > 50) {
            return message.substring(0, 47) + "...";
        }
        return message;
    }

    /**
     * Map entity to response DTO
     */
    private ActivityMessageResponse mapToResponse(ActivityMessage message, Long currentUserId) {
        ActivityMessageResponse response = new ActivityMessageResponse();
        response.setId(message.getId());
        response.setActivityId(message.getActivity().getId());
        response.setUserId(message.getUser().getId());
        response.setUserName(message.getUser().getFullName());
        response.setUserProfilePicture(message.getUser().getProfileImageUrl());
        response.setMessageText(message.getMessageText());
        response.setCreatedAt(message.getCreatedAt());
        response.setIsOwnMessage(message.getUser().getId().equals(currentUserId));
        return response;
    }
}
