package com.gege.activitypartner.service;

import com.gege.activitypartner.dto.UserPhotoResponse;
import com.gege.activitypartner.entity.User;
import com.gege.activitypartner.entity.UserPhoto;
import com.gege.activitypartner.exception.InvalidParticipantActionException;
import com.gege.activitypartner.exception.ResourceNotFoundException;
import com.gege.activitypartner.repository.UserPhotoRepository;
import com.gege.activitypartner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserPhotoService {

    private final UserPhotoRepository userPhotoRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    /**
     * Upload a new photo for a user
     * @param userId the user ID
     * @param file the photo file
     * @return the URL of the uploaded photo
     */
    public String uploadUserPhoto(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Check if user already has 6 photos
        long photoCount = userPhotoRepository.countByUserId(userId);
        if (photoCount >= 6) {
            throw new InvalidParticipantActionException("Maximum 6 photos allowed per user. Please delete one before uploading a new one.");
        }

        // Store the file
        String fileName = fileStorageService.storeFile(file);
        String photoUrl = "/api/users/images/" + fileName;

        // Create and save UserPhoto
        UserPhoto photo = new UserPhoto();
        photo.setUser(user);
        photo.setPhotoUrl(photoUrl);
        photo.setDisplayOrder((int) (photoCount + 1)); // Set display order (1-6)
        photo.setIsProfilePicture(photoCount == 0); // First photo is automatically the profile picture

        UserPhoto savedPhoto = userPhotoRepository.save(photo);

        return mapToResponse(savedPhoto).getPhotoUrl();
    }

    /**
     * Get all photos for a user
     * @param userId the user ID
     * @return list of user photos ordered by display order
     */
    public List<UserPhotoResponse> getUserPhotos(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return userPhotoRepository.findByUserIdOrderByDisplayOrder(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Set a photo as the main profile picture
     * @param userId the user ID
     * @param photoId the photo ID to set as profile picture
     * @return the updated photo
     */
    public UserPhotoResponse setAsProfilePicture(Long userId, Long photoId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        UserPhoto photo = userPhotoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found with id: " + photoId));

        // Verify the photo belongs to the user
        if (!photo.getUser().getId().equals(userId)) {
            throw new InvalidParticipantActionException("This photo does not belong to the user");
        }

        // Unset current profile picture
        userPhotoRepository.findByUserIdAndIsProfilePictureTrue(userId)
                .ifPresent(currentProfile -> {
                    currentProfile.setIsProfilePicture(false);
                    userPhotoRepository.save(currentProfile);
                });

        // Set new profile picture
        photo.setIsProfilePicture(true);
        UserPhoto updatedPhoto = userPhotoRepository.save(photo);

        // Update user's profileImageUrl to match
        user.setProfileImageUrl(photo.getPhotoUrl());
        userRepository.save(user);

        return mapToResponse(updatedPhoto);
    }

    /**
     * Delete a photo
     * @param userId the user ID
     * @param photoId the photo ID to delete
     */
    public void deleteUserPhoto(Long userId, Long photoId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        UserPhoto photo = userPhotoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found with id: " + photoId));

        // Verify the photo belongs to the user
        if (!photo.getUser().getId().equals(userId)) {
            throw new InvalidParticipantActionException("This photo does not belong to the user");
        }

        // If deleting profile picture, set the next photo as profile picture
        if (photo.getIsProfilePicture()) {
            List<UserPhoto> remainingPhotos = userPhotoRepository.findByUserIdOrderByDisplayOrder(userId).stream()
                    .filter(p -> !p.getId().equals(photoId))
                    .collect(Collectors.toList());

            if (!remainingPhotos.isEmpty()) {
                UserPhoto newProfilePicture = remainingPhotos.get(0);
                newProfilePicture.setIsProfilePicture(true);
                userPhotoRepository.save(newProfilePicture);
                user.setProfileImageUrl(newProfilePicture.getPhotoUrl());
                userRepository.save(user);
            } else {
                // No photos left, clear profile image
                user.setProfileImageUrl(null);
                userRepository.save(user);
            }
        }

        // Delete the file from storage
        try {
            String fileName = photo.getPhotoUrl().substring(photo.getPhotoUrl().lastIndexOf('/') + 1);
            fileStorageService.deleteFile(fileName);
        } catch (Exception e) {
            System.err.println("Failed to delete photo file: " + e.getMessage());
        }

        // Delete the photo record
        userPhotoRepository.delete(photo);
    }

    /**
     * Reorder photos (update display order)
     * @param userId the user ID
     * @param photoIds list of photo IDs in desired order
     */
    public void reorderPhotos(Long userId, List<Long> photoIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        for (int i = 0; i < photoIds.size(); i++) {
            Long photoId = photoIds.get(i);
            UserPhoto photo = userPhotoRepository.findById(photoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Photo not found with id: " + photoId));

            // Verify the photo belongs to the user
            if (!photo.getUser().getId().equals(userId)) {
                throw new InvalidParticipantActionException("One or more photos do not belong to the user");
            }

            photo.setDisplayOrder(i + 1);
            userPhotoRepository.save(photo);
        }
    }

    // Helper method to convert UserPhoto to UserPhotoResponse
    private UserPhotoResponse mapToResponse(UserPhoto photo) {
        UserPhotoResponse response = new UserPhotoResponse();
        response.setId(photo.getId());
        response.setPhotoUrl(photo.getPhotoUrl());
        response.setIsProfilePicture(photo.getIsProfilePicture());
        response.setDisplayOrder(photo.getDisplayOrder());
        response.setUploadedAt(photo.getUploadedAt());
        return response;
    }
}
