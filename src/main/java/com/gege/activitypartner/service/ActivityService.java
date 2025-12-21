package com.gege.activitypartner.service;

import com.gege.activitypartner.dto.ActivityRequestDTO;
import com.gege.activitypartner.dto.ActivityResponseDTO;
import com.gege.activitypartner.dto.ActivityUpdateDTO;
import com.gege.activitypartner.dto.UserSimpleResponse;
import com.gege.activitypartner.entity.Activity;
import com.gege.activitypartner.entity.ActivityStatus;
import com.gege.activitypartner.entity.User;
import com.gege.activitypartner.exception.ResourceNotFoundException;
import com.gege.activitypartner.repository.ActivityRepository;
import com.gege.activitypartner.repository.UserRepository;
import com.gege.activitypartner.util.DistanceCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;

    // Create new activity
    public ActivityResponseDTO createActivity(ActivityRequestDTO request, Long creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + creatorId));

        Activity activity = new Activity();
        activity.setTitle(request.getTitle());
        activity.setDescription(request.getDescription());
        activity.setActivityDate(request.getActivityDate());
        activity.setLocation(request.getLocation());
        activity.setPlaceId(request.getPlaceId());
        activity.setLatitude(request.getLatitude() != null ? BigDecimal.valueOf(request.getLatitude()) : null);
        activity.setLongitude(request.getLongitude() != null ? BigDecimal.valueOf(request.getLongitude()) : null);
        activity.setCategory(request.getCategory());
        activity.setTotalSpots(request.getTotalSpots());
        activity.setReservedForFriendsSpots(request.getReservedForFriendsSpots());
        activity.setMinParticipants(request.getMinParticipants());
        activity.setDifficulty(request.getDifficulty());
        activity.setCost(request.getCost());
        activity.setMinAge(request.getMinAge());
        activity.setInterests(request.getInterests());
        activity.setCreator(creator);
        activity.setStatus(ActivityStatus.OPEN);
        activity.setTrending(false);

        Activity savedActivity = activityRepository.save(activity);
        return mapToResponseDTO(savedActivity);
    }

    // Get activity by ID
    @Transactional(readOnly = true)
    public ActivityResponseDTO getActivityById(Long id) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id: " + id));
        return mapToResponseDTO(activity);
    }

    // Get all activities
    @Transactional(readOnly = true)
    public List<ActivityResponseDTO> getAllActivities() {
        return activityRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // Get all activities with distance calculation
    @Transactional(readOnly = true)
    public List<ActivityResponseDTO> getAllActivitiesWithDistance(Double userLatitude, Double userLongitude) {
        BigDecimal userLat = BigDecimal.valueOf(userLatitude);
        BigDecimal userLon = BigDecimal.valueOf(userLongitude);
        return activityRepository.findAll().stream()
                .map(activity -> mapToResponseDTO(activity, userLat, userLon))
                .collect(Collectors.toList());
    }

    // Get activities by creator
    @Transactional(readOnly = true)
    public List<ActivityResponseDTO> getActivitiesByCreator(Long creatorId) {
        return activityRepository.findByCreatorId(creatorId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // Get my activities (current user) filtered by status
    @Transactional(readOnly = true)
    public List<ActivityResponseDTO> getMyActivities(Long creatorId, ActivityStatus status) {
        List<Activity> activities;
        if (status == null) {
            // Return all activities if no status filter
            activities = activityRepository.findByCreatorId(creatorId);
        } else {
            // Return activities filtered by status
            activities = activityRepository.findByCreatorId(creatorId).stream()
                    .filter(a -> a.getStatus() == status)
                    .collect(Collectors.toList());
        }
        return activities.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // Get activities by category
    @Transactional(readOnly = true)
    public List<ActivityResponseDTO> getActivitiesByCategory(String category) {
        return activityRepository.findByCategory(category).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // Get available upcoming activities
    @Transactional(readOnly = true)
    public List<ActivityResponseDTO> getAvailableUpcomingActivities(Long userId) {

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        List<ActivityResponseDTO> activityResponseDTOS = activityRepository.findAvailableUpcomingActivities(LocalDateTime.now()).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());

        List<ActivityResponseDTO> responseDTOS = new ArrayList<>();
for (ActivityResponseDTO activityResponseDTO : activityResponseDTOS){
    // Only calculate distance if user has location set
    if (currentUser.getLatitude() != null && currentUser.getLongitude() != null) {
        activityResponseDTO.setDistance(DistanceCalculator.calculateDistance(currentUser.getLatitude(), currentUser.getLongitude(), activityResponseDTO.getLatitude(), activityResponseDTO.getLongitude()));
    } else {
        activityResponseDTO.setDistance(null);
    }
    responseDTOS.add(activityResponseDTO);
}


        return responseDTOS;
    }

    // Get trending activities
    @Transactional(readOnly = true)
    public List<ActivityResponseDTO> getTrendingActivities() {
        return activityRepository.findByTrendingTrue().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // Get nearby activities within radius
    @Transactional(readOnly = true)
    public List<ActivityResponseDTO> getNearbyActivities(Double userLatitude, Double userLongitude, Double radiusKm) {
        List<Activity> allActivities = activityRepository.findAvailableUpcomingActivities(LocalDateTime.now());
        BigDecimal userLat = BigDecimal.valueOf(userLatitude);
        BigDecimal userLon = BigDecimal.valueOf(userLongitude);

        return allActivities.stream()
                .filter(activity -> activity.getLatitude() != null && activity.getLongitude() != null)
                .filter(activity -> {
                    double distance = DistanceCalculator.calculateDistance(
                        userLat, userLon,
                        activity.getLatitude(),
                        activity.getLongitude()
                    );
                    return distance <= radiusKm;
                })
                .map(activity -> mapToResponseDTO(activity, userLat, userLon))
                .collect(Collectors.toList());
    }

    // Get recommended activities based on user's interests
    @Transactional(readOnly = true)
    public List<ActivityResponseDTO> getRecommendedActivities(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        List<String> userInterests = user.getInterests();

        // If user has no interests, return empty or trending activities
        if (userInterests == null || userInterests.isEmpty()) {
            return getTrendingActivities();
        }

        List<Activity> allActivities = activityRepository.findAll();
        BigDecimal userLat = user.getLatitude();
        BigDecimal userLon = user.getLongitude();

        // Filter and score activities based on interest matches
        return allActivities.stream()
                .filter(activity -> activity.getStatus() == ActivityStatus.OPEN) // Only open activities
                .filter(activity -> !activity.getCreator().getId().equals(userId)) // Exclude user's own activities
                .filter(activity -> activity.getAvailableSpots() > 0) // Has available spots
                .sorted((a1, a2) -> {
                    // Score by interest matches (higher is better)
                    int score1 = countInterestMatches(a1.getInterests(), userInterests);
                    int score2 = countInterestMatches(a2.getInterests(), userInterests);
                    if (score2 != score1) {
                        return Integer.compare(score2, score1); // Sort by score descending
                    }
                    // If same score, sort by activity date (nearest first)
                    return a1.getActivityDate().compareTo(a2.getActivityDate());
                })
                .map(activity -> {
                    // Calculate distance if user location is available
                    if (userLat != null && userLon != null) {
                        return mapToResponseDTO(activity, userLat, userLon);
                    } else {
                        return mapToResponseDTO(activity);
                    }
                })
                .collect(Collectors.toList());
    }

    // Helper method to count matching interests between activity and user
    private int countInterestMatches(List<String> activityInterests, List<String> userInterests) {
        if (activityInterests == null || activityInterests.isEmpty()) {
            return 0;
        }

        int matches = 0;
        for (String interest : activityInterests) {
            if (userInterests.contains(interest)) {
                matches++;
            }
        }
        return matches;
    }

    // Update activity
    public ActivityResponseDTO updateActivity(Long id, ActivityUpdateDTO updateDTO, Long userId) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id: " + id));

        // Check if user is the creator
        if (!activity.getCreator().getId().equals(userId)) {
            throw new IllegalStateException("Only the creator can update this activity");
        }

        // Update only non-null fields
        if (updateDTO.getTitle() != null) {
            activity.setTitle(updateDTO.getTitle());
        }
        if (updateDTO.getDescription() != null) {
            activity.setDescription(updateDTO.getDescription());
        }
        if (updateDTO.getActivityDate() != null) {
            activity.setActivityDate(updateDTO.getActivityDate());
        }
        if (updateDTO.getLocation() != null) {
            activity.setLocation(updateDTO.getLocation());
        }
        if (updateDTO.getPlaceId() != null) {
            activity.setPlaceId(updateDTO.getPlaceId());
        }
        if (updateDTO.getLatitude() != null) {
            activity.setLatitude(BigDecimal.valueOf(updateDTO.getLatitude()));
        }
        if (updateDTO.getLongitude() != null) {
            activity.setLongitude(BigDecimal.valueOf(updateDTO.getLongitude()));
        }
        if (updateDTO.getCategory() != null) {
            activity.setCategory(updateDTO.getCategory());
        }
        if (updateDTO.getTotalSpots() != null) {
            activity.setTotalSpots(updateDTO.getTotalSpots());
        }
        if (updateDTO.getReservedForFriendsSpots() != null) {
            activity.setReservedForFriendsSpots(updateDTO.getReservedForFriendsSpots());
        }
        if (updateDTO.getMinParticipants() != null) {
            activity.setMinParticipants(updateDTO.getMinParticipants());
        }
        if (updateDTO.getDifficulty() != null) {
            activity.setDifficulty(updateDTO.getDifficulty());
        }
        if (updateDTO.getCost() != null) {
            activity.setCost(updateDTO.getCost());
        }
        if (updateDTO.getMinAge() != null) {
            activity.setMinAge(updateDTO.getMinAge());
        }
        if (updateDTO.getInterests() != null) {
            activity.setInterests(updateDTO.getInterests());
        }

        Activity updatedActivity = activityRepository.save(activity);
        return mapToResponseDTO(updatedActivity);
    }

    // Cancel activity
    public ActivityResponseDTO cancelActivity(Long id, Long userId) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id: " + id));

        // Check if user is the creator
        if (!activity.getCreator().getId().equals(userId)) {
            throw new IllegalStateException("Only the creator can cancel this activity");
        }

        activity.setStatus(ActivityStatus.CANCELLED);
        Activity updatedActivity = activityRepository.save(activity);
        return mapToResponseDTO(updatedActivity);
    }

    // Complete activity
    public ActivityResponseDTO completeActivity(Long id, Long userId) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id: " + id));

        // Check if user is the creator
        if (!activity.getCreator().getId().equals(userId)) {
            throw new IllegalStateException("Only the creator can complete this activity");
        }

        activity.setStatus(ActivityStatus.COMPLETED);
        Activity updatedActivity = activityRepository.save(activity);
        return mapToResponseDTO(updatedActivity);
    }

    // Delete activity
    public void deleteActivity(Long id, Long userId) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id: " + id));

        // Check if user is the creator
        if (!activity.getCreator().getId().equals(userId)) {
            throw new IllegalStateException("Only the creator can delete this activity");
        }

        activityRepository.delete(activity);
    }

    // Helper method to map Activity to ActivityResponseDTO
    private ActivityResponseDTO mapToResponseDTO(Activity activity) {
        return mapToResponseDTO(activity, null, null);
    }

    // Helper method to map Activity to ActivityResponseDTO with distance calculation
    private ActivityResponseDTO mapToResponseDTO(Activity activity, BigDecimal userLatitude, BigDecimal userLongitude) {
        ActivityResponseDTO dto = new ActivityResponseDTO();
        dto.setId(activity.getId());
        dto.setTitle(activity.getTitle());
        dto.setDescription(activity.getDescription());
        dto.setActivityDate(activity.getActivityDate());
        dto.setLocation(activity.getLocation());
        dto.setPlaceId(activity.getPlaceId());
        dto.setLatitude(activity.getLatitude() != null ? activity.getLatitude() : null);
        dto.setLongitude(activity.getLongitude() != null ? activity.getLongitude(): null);

        // Calculate distance if user coordinates are provided
        if (userLatitude != null && userLongitude != null &&
            activity.getLatitude() != null && activity.getLongitude() != null) {
            double distance = DistanceCalculator.calculateDistance(
                userLatitude, userLongitude,
                activity.getLatitude(), activity.getLongitude()
            );
            dto.setDistance(distance);
        }

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
        dto.setInterests(activity.getInterests());

        // Map creator user object
        UserSimpleResponse creatorResponse = new UserSimpleResponse();
        creatorResponse.setId(activity.getCreator().getId());
        creatorResponse.setFullName(activity.getCreator().getFullName());
        creatorResponse.setProfileImageUrl(activity.getCreator().getProfileImageUrl());
        creatorResponse.setRating(activity.getCreator().getRating());
        creatorResponse.setBadge(activity.getCreator().getBadge());
        dto.setCreator(creatorResponse);

        dto.setParticipantsCount(activity.getParticipants().size());
        dto.setCreatedAt(activity.getCreatedAt());
        dto.setUpdatedAt(activity.getUpdatedAt());
        return dto;
    }
}
