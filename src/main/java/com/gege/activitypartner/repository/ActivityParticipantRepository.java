package com.gege.activitypartner.repository;

import com.gege.activitypartner.entity.ActivityParticipant;
import com.gege.activitypartner.entity.ParticipantStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityParticipantRepository extends JpaRepository<ActivityParticipant, Long> {

  // Find participation by activity and user
  Optional<ActivityParticipant> findByActivityIdAndUserId(Long activityId, Long userId);

  // Check if user already participates in activity
  boolean existsByActivityIdAndUserId(Long activityId, Long userId);

  // Find all participants for an activity
  List<ActivityParticipant> findByActivityId(Long activityId);

  // Find all participants by status for an activity
  List<ActivityParticipant> findByActivityIdAndStatus(Long activityId, ParticipantStatus status);

  // Get all activities a user is participating in
  List<ActivityParticipant> findByUserId(Long userId);

  // Get user's participations by status
  List<ActivityParticipant> findByUserIdAndStatus(Long userId, ParticipantStatus status);

  // Count participants by status for an activity
  @Query(
      "SELECT COUNT(ap) FROM ActivityParticipant ap WHERE ap.activity.id = :activityId AND ap.status = :status")
  Long countByActivityIdAndStatus(
      @Param("activityId") Long activityId, @Param("status") ParticipantStatus status);

  // Get interested users for an activity (for creator only)
  @Query(
      "SELECT ap FROM ActivityParticipant ap "
          + "JOIN FETCH ap.user "
          + "WHERE ap.activity.id = :activityId AND ap.status = 'INTERESTED' "
          + "ORDER BY ap.joinedAt ASC")
  List<ActivityParticipant> findInterestedUsersByActivityId(@Param("activityId") Long activityId);

  // Get all joined participants with user details
  @Query(
      "SELECT ap FROM ActivityParticipant ap "
          + "JOIN FETCH ap.user "
          + "WHERE ap.activity.id = :activityId AND ap.status = 'JOINED' "
          + "ORDER BY ap.updatedAt DESC")
  List<ActivityParticipant> findJoinedUsersByActivityId(@Param("activityId") Long activityId);

  // Get user's upcoming participations (joined only)
  @Query(
      "SELECT ap FROM ActivityParticipant ap "
          + "JOIN FETCH ap.activity a "
          + "WHERE ap.user.id = :userId "
          + "AND ap.status = 'JOINED' "
          + "AND a.activityDate > CURRENT_TIMESTAMP "
          + "ORDER BY a.activityDate ASC")
  List<ActivityParticipant> findUpcomingJoinedActivitiesByUserId(@Param("userId") Long userId);

  // Count interested users for an activity
  @Query(
      "SELECT COUNT(ap) FROM ActivityParticipant ap WHERE ap.activity.id = :activityId AND ap.status = 'INTERESTED'")
  Long countInterestedByActivityId(@Param("activityId") Long activityId);

  // Find all participants with activity and user eagerly fetched
  @Query(
      "SELECT ap FROM ActivityParticipant ap "
          + "JOIN FETCH ap.activity "
          + "JOIN FETCH ap.user "
          + "WHERE ap.id = :id")
  Optional<ActivityParticipant> findByIdWithDetails(@Param("id") Long id);

  // Count total application attempts by user for an activity (including withdrawn/declined)
  @Query(
      "SELECT COUNT(ap) FROM ActivityParticipant ap "
          + "WHERE ap.activity.id = :activityId AND ap.user.id = :userId")
  Long countApplicationAttempts(@Param("activityId") Long activityId, @Param("userId") Long userId);
}
