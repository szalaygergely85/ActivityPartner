package com.gege.activitypartner.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FirebaseMessagingService {

  /** Send a notification to a single device */
  public boolean sendNotification(
      String fcmToken, String title, String body, Map<String, String> data) {
    if (FirebaseApp.getApps().isEmpty()) {
      log.warn("Firebase not initialized. Notification not sent: {}", title);
      return false;
    }

    if (fcmToken == null || fcmToken.isEmpty()) {
      log.warn("FCM token is null or empty. Notification not sent: {}", title);
      return false;
    }

    try {
      // Build notification
      Notification notification = Notification.builder().setTitle(title).setBody(body).build();

      // Build message
      Message.Builder messageBuilder =
          Message.builder().setToken(fcmToken).setNotification(notification);

      // Add data payload if provided
      if (data != null && !data.isEmpty()) {
        messageBuilder.putAllData(data);
      }

      // Send message
      String response = FirebaseMessaging.getInstance().send(messageBuilder.build());
      log.info(
          "Successfully sent notification to token: {}... Response: {}",
          fcmToken.substring(0, Math.min(10, fcmToken.length())),
          response);
      return true;

    } catch (FirebaseMessagingException e) {
      log.error(
          "Failed to send notification to token: {}... Error: {}",
          fcmToken.substring(0, Math.min(10, fcmToken.length())),
          e.getMessage());
      return false;
    }
  }

  /** Send notification with default data (convenience method) */
  public boolean sendNotification(String fcmToken, String title, String body) {
    return sendNotification(fcmToken, title, body, null);
  }

  /** Send notification to multiple devices */
  public Map<String, Boolean> sendNotificationToMultipleDevices(
      List<String> fcmTokens, String title, String body, Map<String, String> data) {
    Map<String, Boolean> results = new HashMap<>();

    for (String token : fcmTokens) {
      boolean success = sendNotification(token, title, body, data);
      results.put(token, success);
    }

    return results;
  }

  /** Send notification with navigation data for deep linking */
  public boolean sendNotificationWithNavigation(
      String fcmToken, String title, String body, String screen, Long entityId) {
    Map<String, String> data = new HashMap<>();
    data.put("screen", screen);
    if (entityId != null) {
      data.put("entityId", entityId.toString());
    }

    return sendNotification(fcmToken, title, body, data);
  }

  /** Subscribe a token to a topic */
  public void subscribeToTopic(String fcmToken, String topic) {
    if (FirebaseApp.getApps().isEmpty()) {
      log.warn("Firebase not initialized. Cannot subscribe to topic: {}", topic);
      return;
    }

    try {
      TopicManagementResponse response =
          FirebaseMessaging.getInstance().subscribeToTopic(List.of(fcmToken), topic);
      log.info(
          "Successfully subscribed to topic '{}': {} success, {} failure",
          topic,
          response.getSuccessCount(),
          response.getFailureCount());
    } catch (FirebaseMessagingException e) {
      log.error("Failed to subscribe to topic '{}': {}", topic, e.getMessage());
    }
  }

  /** Unsubscribe a token from a topic */
  public void unsubscribeFromTopic(String fcmToken, String topic) {
    if (FirebaseApp.getApps().isEmpty()) {
      log.warn("Firebase not initialized. Cannot unsubscribe from topic: {}", topic);
      return;
    }

    try {
      TopicManagementResponse response =
          FirebaseMessaging.getInstance().unsubscribeFromTopic(List.of(fcmToken), topic);
      log.info(
          "Successfully unsubscribed from topic '{}': {} success, {} failure",
          topic,
          response.getSuccessCount(),
          response.getFailureCount());
    } catch (FirebaseMessagingException e) {
      log.error("Failed to unsubscribe from topic '{}': {}", topic, e.getMessage());
    }
  }

  /** Send notification to a topic */
  public boolean sendNotificationToTopic(
      String topic, String title, String body, Map<String, String> data) {
    if (FirebaseApp.getApps().isEmpty()) {
      log.warn("Firebase not initialized. Notification to topic not sent: {}", topic);
      return false;
    }

    try {
      Notification notification = Notification.builder().setTitle(title).setBody(body).build();

      Message.Builder messageBuilder =
          Message.builder().setTopic(topic).setNotification(notification);

      if (data != null && !data.isEmpty()) {
        messageBuilder.putAllData(data);
      }

      String response = FirebaseMessaging.getInstance().send(messageBuilder.build());
      log.info("Successfully sent notification to topic '{}'. Response: {}", topic, response);
      return true;

    } catch (FirebaseMessagingException e) {
      log.error("Failed to send notification to topic '{}': {}", topic, e.getMessage());
      return false;
    }
  }
}
