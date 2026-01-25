package com.gege.activitypartner.service;

import com.gege.activitypartner.entity.Activity;
import com.gege.activitypartner.entity.ActivityParticipant;
import com.gege.activitypartner.entity.ActivityStatus;
import com.gege.activitypartner.entity.NotificationType;
import com.gege.activitypartner.repository.ActivityRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivitySchedulerService {

  private final ActivityRepository activityRepository;
  private final NotificationService notificationService;

  /**
   * Scheduled task that runs every 5 minutes to mark expired activities as COMPLETED. An activity
   * is considered expired when its activityDate has passed and it's still in OPEN status.
   *
   * <p>This cron job addresses the issue where activities don't automatically transition to
   * COMPLETED status after their scheduled time expires.
   */
  @Scheduled(cron = "0 */5 * * * ?")
  @Transactional
  public void markExpiredActivitiesAsCompleted() {
    try {
      LocalDateTime now = LocalDateTime.now();

      // Find all OPEN activities where activityDate has passed
      List<Activity> expiredActivities =
          activityRepository.findByStatusAndActivityDateBefore(ActivityStatus.OPEN, now);

      if (!expiredActivities.isEmpty()) {
        expiredActivities.forEach(
            activity -> {
              activity.setStatus(ActivityStatus.COMPLETED);
              log.info(
                  "Marked activity '{}' (ID: {}) as COMPLETED. Activity date was: {}",
                  activity.getTitle(),
                  activity.getId(),
                  activity.getActivityDate());

              // Send notifications to all participants to leave reviews
              sendActivityCompletedNotifications(activity);
            });

        activityRepository.saveAll(expiredActivities);
        log.info(
            "Successfully marked {} expired activities as COMPLETED", expiredActivities.size());
      }
    } catch (Exception e) {
      log.error("Error in markExpiredActivitiesAsCompleted scheduler", e);
    }
  }

  /**
   * Alternative scheduled task that runs daily at 2 AM to clean up very old cancelled/completed
   * activities. This can help with database maintenance and performance.
   *
   * <p>Cron expression: "0 0 2 * * ?" - 0: seconds (0) - 0: minutes (0) - 2: hour (2 AM) - *: every
   * day of month - *: every month - ?: day of week (ignored)
   */
  @Scheduled(cron = "0 0 2 * * ?")
  @Transactional
  public void performDailyActivityMaintenance() {
    try {
      LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

      // You can add cleanup logic here if needed
      // For example, archive very old completed activities
      log.info("Daily activity maintenance completed at: {}", LocalDateTime.now());
    } catch (Exception e) {
      log.error("Error in performDailyActivityMaintenance scheduler", e);
    }
  }

  /**
   * Scheduled task that runs every 5 minutes to send reminder notifications for activities starting
   * within the next hour. Reminders are sent to both the activity creator and all confirmed
   * participants.
   */
  @Scheduled(cron = "0 */5 * * * ?")
  @Transactional
  public void sendActivityReminders() {
    try {
      LocalDateTime now = LocalDateTime.now();
      LocalDateTime oneHourFromNow = now.plusHours(1);

      // Find OPEN activities starting in the next hour that haven't had reminders sent
      List<Activity> activitiesNeedingReminder =
          activityRepository.findActivitiesNeedingReminder(
              ActivityStatus.OPEN, now, oneHourFromNow);

      if (!activitiesNeedingReminder.isEmpty()) {
        activitiesNeedingReminder.forEach(
            activity -> {
              sendActivityReminderNotifications(activity);
              activity.setReminderSent(true);
              log.info(
                  "Sent reminder for activity '{}' (ID: {}) starting at: {}",
                  activity.getTitle(),
                  activity.getId(),
                  activity.getActivityDate());
            });

        activityRepository.saveAll(activitiesNeedingReminder);
        log.info(
            "Successfully sent reminders for {} upcoming activities",
            activitiesNeedingReminder.size());
      }
    } catch (Exception e) {
      log.error("Error in sendActivityReminders scheduler", e);
    }
  }

  /**
   * Sends reminder notifications to the creator and all confirmed participants of an upcoming
   * activity.
   *
   * @param activity the activity starting soon
   */
  private void sendActivityReminderNotifications(Activity activity) {
    try {
      String timeUntilStart = getTimeUntilStart(activity.getActivityDate());

      // Send notification to the activity creator
      notificationService.createAndSendNotification(
          activity.getCreator(),
          "Activity Starting Soon!",
          "Your activity \""
              + activity.getTitle()
              + "\" is starting "
              + timeUntilStart
              + " at "
              + activity.getLocation()
              + ". Get ready!",
          NotificationType.ACTIVITY_REMINDER,
          activity.getId(),
          null,
          null);
      log.debug(
          "Sent activity reminder to creator: {} for activity: {}",
          activity.getCreator().getId(),
          activity.getId());

      // Send notification to each confirmed participant
      for (ActivityParticipant participant : activity.getParticipants()) {
        if (participant.getStatus() == com.gege.activitypartner.entity.ParticipantStatus.JOINED) {
          notificationService.createAndSendNotification(
              participant.getUser(),
              "Activity Starting Soon!",
              "The activity \""
                  + activity.getTitle()
                  + "\" is starting "
                  + timeUntilStart
                  + " at "
                  + activity.getLocation()
                  + ". Don't forget!",
              NotificationType.ACTIVITY_REMINDER,
              activity.getId(),
              participant.getId(),
              null);

          log.debug(
              "Sent activity reminder to participant: {} for activity: {}",
              participant.getUser().getId(),
              activity.getId());
        }
      }

      log.info(
          "Successfully sent reminder notifications for activity: {} to creator + participants",
          activity.getId());
    } catch (Exception e) {
      log.error(
          "Error sending activity reminder notifications for activity ID: {}", activity.getId(), e);
    }
  }

  /**
   * Calculates a human-readable time until the activity starts.
   *
   * @param activityDate the activity start time
   * @return a string like "in 45 minutes" or "in about 1 hour"
   */
  private String getTimeUntilStart(LocalDateTime activityDate) {
    long minutesUntil = java.time.Duration.between(LocalDateTime.now(), activityDate).toMinutes();

    if (minutesUntil <= 30) {
      return "in " + minutesUntil + " minutes";
    } else if (minutesUntil <= 60) {
      return "in about 1 hour";
    } else {
      return "soon";
    }
  }

  /**
   * Sends notifications to all participants of a completed activity, prompting them to leave
   * reviews for other participants.
   *
   * @param activity the activity that was just completed
   */
  private void sendActivityCompletedNotifications(Activity activity) {
    try {
      // Send ONE notification to the activity creator with combined message
      notificationService.createAndSendNotification(
          activity.getCreator(),
          "Activity Completed",
          "Your activity \""
              + activity.getTitle()
              + "\" has been marked as completed. Thank you for organizing! Don't forget to leave reviews for your participants.",
          NotificationType.ACTIVITY_COMPLETED,
          activity.getId(),
          null,
          null);
      log.debug(
          "Sent activity completion notification to creator: {} for activity: {}",
          activity.getCreator().getId(),
          activity.getId());

      // Send notification to each participant to leave reviews
      for (ActivityParticipant participant : activity.getParticipants()) {
        notificationService.createAndSendNotification(
            participant.getUser(),
            "Activity Completed - Leave a Review",
            "The activity \""
                + activity.getTitle()
                + "\" has ended. Please leave reviews for other participants!",
            NotificationType.ACTIVITY_COMPLETED,
            activity.getId(),
            participant.getId(),
            null);

        log.debug(
            "Sent activity completion notification to participant: {} for activity: {}",
            participant.getUser().getId(),
            activity.getId());
      }

      log.info(
          "Successfully sent completion notifications for activity: {} to creator + {} participants",
          activity.getId(),
          activity.getParticipants().size());
    } catch (Exception e) {
      log.error(
          "Error sending activity completion notifications for activity ID: {}",
          activity.getId(),
          e);
    }
  }
}
