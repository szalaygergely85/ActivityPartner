package com.gege.activitypartner.repository;

import com.gege.activitypartner.entity.ActivityMessage;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityMessageRepository extends JpaRepository<ActivityMessage, Long> {

  @Query(
      "SELECT m FROM ActivityMessage m WHERE m.activity.id = :activityId AND m.isDeleted = false ORDER BY m.createdAt ASC")
  List<ActivityMessage> findByActivityIdAndNotDeleted(@Param("activityId") Long activityId);

  @Query(
      "SELECT m FROM ActivityMessage m WHERE m.activity.id = :activityId AND m.isDeleted = false AND m.createdAt > :since ORDER BY m.createdAt ASC")
  List<ActivityMessage> findByActivityIdSince(
      @Param("activityId") Long activityId, @Param("since") LocalDateTime since);

  @Query(
      "SELECT COUNT(m) FROM ActivityMessage m WHERE m.activity.id = :activityId AND m.isDeleted = false")
  Long countByActivityId(@Param("activityId") Long activityId);
}
