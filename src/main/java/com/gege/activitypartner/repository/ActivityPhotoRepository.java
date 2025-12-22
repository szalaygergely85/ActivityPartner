package com.gege.activitypartner.repository;

import com.gege.activitypartner.entity.ActivityPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityPhotoRepository extends JpaRepository<ActivityPhoto, Long> {

    // Find all photos for an activity, ordered by display order
    @Query("SELECT ap FROM ActivityPhoto ap " +
           "JOIN FETCH ap.user " +
           "WHERE ap.activity.id = :activityId " +
           "ORDER BY ap.displayOrder ASC, ap.uploadedAt ASC")
    List<ActivityPhoto> findByActivityIdWithUser(@Param("activityId") Long activityId);

    // Count photos for an activity
    Long countByActivityId(Long activityId);

    // Find a specific photo with user details
    @Query("SELECT ap FROM ActivityPhoto ap " +
           "JOIN FETCH ap.user " +
           "WHERE ap.id = :photoId")
    Optional<ActivityPhoto> findByIdWithUser(@Param("photoId") Long photoId);

    // Find all photos uploaded by a specific user for an activity
    List<ActivityPhoto> findByActivityIdAndUserId(Long activityId, Long userId);

    // Count photos uploaded by a specific user for an activity
    Long countByActivityIdAndUserId(Long activityId, Long userId);
}
