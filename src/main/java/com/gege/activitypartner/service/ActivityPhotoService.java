package com.gege.activitypartner.service;

import com.gege.activitypartner.dto.ActivityGalleryAccessResponse;
import com.gege.activitypartner.dto.ActivityPhotoResponse;
import com.gege.activitypartner.entity.Activity;
import com.gege.activitypartner.entity.ActivityParticipant;
import com.gege.activitypartner.entity.ActivityPhoto;
import com.gege.activitypartner.entity.ParticipantStatus;
import com.gege.activitypartner.entity.User;
import com.gege.activitypartner.exception.InvalidParticipantActionException;
import com.gege.activitypartner.exception.ResourceNotFoundException;
import com.gege.activitypartner.repository.ActivityParticipantRepository;
import com.gege.activitypartner.repository.ActivityPhotoRepository;
import com.gege.activitypartner.repository.ActivityRepository;
import com.gege.activitypartner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ActivityPhotoService {

    private final ActivityPhotoRepository activityPhotoRepository;
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final ActivityParticipantRepository activityParticipantRepository;
    private final FileStorageService fileStorageService;

    @Value("${activity.gallery.min-photos:3}")
    private int minPhotosPerActivity;

    @Value("${activity.gallery.max-photos:40}")
    private int maxPhotosPerActivity;

    /**
     * Check if a user has access to view the activity gallery.
     * Gallery is accessible only to participants who joined the activity after the event has ended
     */
    public ActivityGalleryAccessResponse checkGalleryAccess(Long activityId, Long userId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id: " + activityId));


        // Check if user was a participant who joined
        ActivityParticipant participant = activityParticipantRepository
                .findByActivityIdAndUserId(activityId, userId)
                .orElse(null);

        boolean wasParticipant = participant != null &&
                (participant.getStatus() == ParticipantStatus.ACCEPTED ||
                 participant.getStatus() == ParticipantStatus.JOINED);

        // Check if activity has ended
        boolean hasEnded = activity.getActivityDate().isBefore(LocalDateTime.now());

        // Count current photos
        long photoCount = activityPhotoRepository.countByActivityId(activityId);

        ActivityGalleryAccessResponse response = new ActivityGalleryAccessResponse();
        response.setPhotoCount((int) photoCount);
        response.setMaxPhotos(maxPhotosPerActivity);

        if (!wasParticipant) {
            response.setHasAccess(false);
            response.setCanUpload(false);
            response.setReason("Only participants who joined this activity can view the gallery");
            return response;
        }

        if (!hasEnded) {
            response.setHasAccess(false);
            response.setCanUpload(false);
            response.setReason("Gallery will be available after the activity ends");
            return response;
        }

        // User has access
        response.setHasAccess(true);
        response.setCanUpload(photoCount < maxPhotosPerActivity);
        response.setReason(null);
        return response;
    }

    /**
     * Upload photos for an activity
     * Only participants who joined can upload photos, and only after the activity has ended
     */
    public List<ActivityPhotoResponse> uploadActivityPhotos(Long activityId, Long userId, List<MultipartFile> files) {
        // Check access
        ActivityGalleryAccessResponse access = checkGalleryAccess(activityId, userId);
        if (!access.getHasAccess()) {
            throw new InvalidParticipantActionException(access.getReason());
        }

        if (!access.getCanUpload()) {
            throw new InvalidParticipantActionException("Maximum number of photos reached for this activity");
        }

        // Validate number of photos
        if (files.isEmpty()) {
            throw new InvalidParticipantActionException("At least one photo is required");
        }

        long currentPhotoCount = activityPhotoRepository.countByActivityId(activityId);
        if (currentPhotoCount + files.size() > maxPhotosPerActivity) {
            throw new InvalidParticipantActionException(
                    "Cannot upload " + files.size() + " photos. Maximum total is " + maxPhotosPerActivity +
                    " photos per activity. Current count: " + currentPhotoCount);
        }

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Upload all photos
        List<ActivityPhoto> uploadedPhotos = files.stream()
                .map(file -> {
                    String fileName = fileStorageService.storeFile(file);
                    String photoUrl = "/api/users/images/" + fileName;

                    ActivityPhoto photo = new ActivityPhoto();
                    photo.setActivity(activity);
                    photo.setUser(user);
                    photo.setPhotoUrl(photoUrl);
                    photo.setDisplayOrder((int) (activityPhotoRepository.countByActivityId(activityId) + 1));

                    return activityPhotoRepository.save(photo);
                })
                .collect(Collectors.toList());

        return uploadedPhotos.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all photos for an activity
     * Only accessible to participants who joined
     */
    public List<ActivityPhotoResponse> getActivityPhotos(Long activityId, Long userId) {
        // Check access
        ActivityGalleryAccessResponse access = checkGalleryAccess(activityId, userId);
        if (!access.getHasAccess()) {
            throw new InvalidParticipantActionException(access.getReason());
        }

        return activityPhotoRepository.findByActivityIdWithUser(activityId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Delete a photo
     * Users can only delete their own photos
     */
    public void deleteActivityPhoto(Long activityId, Long photoId, Long userId) {
        ActivityPhoto photo = activityPhotoRepository.findByIdWithUser(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found with id: " + photoId));

        // Verify photo belongs to the activity
        if (!photo.getActivity().getId().equals(activityId)) {
            throw new InvalidParticipantActionException("Photo does not belong to this activity");
        }

        // Verify photo belongs to the user (or user is activity creator)
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found"));

        boolean isPhotoOwner = photo.getUser().getId().equals(userId);
        boolean isActivityCreator = activity.getCreator().getId().equals(userId);

        if (!isPhotoOwner && !isActivityCreator) {
            throw new InvalidParticipantActionException("You can only delete your own photos");
        }

        // Extract filename from URL and delete file
        String photoUrl = photo.getPhotoUrl();
        String fileName = photoUrl.substring(photoUrl.lastIndexOf('/') + 1);
        fileStorageService.deleteFile(fileName);

        activityPhotoRepository.delete(photo);
    }

    /**
     * Get photo count for an activity
     */
    public Long getPhotoCount(Long activityId) {
        return activityPhotoRepository.countByActivityId(activityId);
    }

    /**
     * Map ActivityPhoto entity to ActivityPhotoResponse DTO
     */
    private ActivityPhotoResponse mapToResponse(ActivityPhoto photo) {
        ActivityPhotoResponse response = new ActivityPhotoResponse();
        response.setId(photo.getId());
        response.setActivityId(photo.getActivity().getId());
        response.setUserId(photo.getUser().getId());
        response.setUserName(photo.getUser().getFullName());
        response.setUserAvatar(photo.getUser().getProfileImageUrl());
        response.setPhotoUrl(photo.getPhotoUrl());
        response.setDisplayOrder(photo.getDisplayOrder());
        response.setUploadedAt(photo.getUploadedAt());
        return response;
    }
}
