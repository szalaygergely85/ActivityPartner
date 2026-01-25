package com.gege.activitypartner.repository;

import com.gege.activitypartner.entity.Activity;
import com.gege.activitypartner.entity.ActivityStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

  // Find all activities by creator
  List<Activity> findByCreatorId(Long creatorId);

  // Find activities by status
  List<Activity> findByStatus(ActivityStatus status);

  // Find activities by category
  List<Activity> findByCategory(String category);

  // Find trending activities
  List<Activity> findByTrendingTrue();

  // Find upcoming activities (after current date and OPEN status)
  List<Activity> findByActivityDateAfterAndStatus(LocalDateTime date, ActivityStatus status);

  // Find activities by category and status
  List<Activity> findByCategoryAndStatus(String category, ActivityStatus status);

  // Custom query: Find activities with available spots
  @Query(
      "SELECT a FROM Activity a WHERE a.status = 'OPEN' AND a.activityDate > :now ORDER BY a.activityDate ASC")
  List<Activity> findAvailableUpcomingActivities(@Param("now") LocalDateTime now);

  // Find activities by location containing (case insensitive)
  List<Activity> findByLocationContainingIgnoreCase(String location);

  // Find activities by cost range
  List<Activity> findByCostBetween(Double minCost, Double maxCost);

  // Find free activities (cost = 0)
  List<Activity> findByCost(Double cost);

  // Find expired activities (activities with past activityDate and OPEN status)
  List<Activity> findByStatusAndActivityDateBefore(ActivityStatus status, LocalDateTime date);

  // Find activities needing reminder (within time range, OPEN status, reminder not sent)
  @Query(
      "SELECT a FROM Activity a WHERE a.status = :status "
          + "AND a.activityDate BETWEEN :startTime AND :endTime "
          + "AND a.reminderSent = false")
  List<Activity> findActivitiesNeedingReminder(
      @Param("status") ActivityStatus status,
      @Param("startTime") LocalDateTime startTime,
      @Param("endTime") LocalDateTime endTime);
}
