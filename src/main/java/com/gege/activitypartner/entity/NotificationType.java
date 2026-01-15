package com.gege.activitypartner.entity;

public enum NotificationType {
  // Activity-related notifications
  ACTIVITY_CREATED, // New activity by someone you follow
  ACTIVITY_UPDATED, // Activity you joined was updated
  ACTIVITY_CANCELLED, // Activity you joined was cancelled
  ACTIVITY_COMPLETED, // Activity you joined was completed
  ACTIVITY_REMINDER, // Reminder before activity starts

  // Participation-related notifications
  PARTICIPANT_INTERESTED, // Someone expressed interest in your activity
  PARTICIPANT_ACCEPTED, // Your interest was accepted
  PARTICIPANT_DECLINED, // Your interest was declined
  PARTICIPANT_JOINED, // Someone confirmed joining your activity
  PARTICIPANT_LEFT, // Someone left your activity

  // Review-related notifications
  REVIEW_RECEIVED, // You received a new review

  // Message-related notifications
  NEW_MESSAGE, // New message in activity chat

  // Report-related notifications
  REPORT_SUBMITTED, // Your report was submitted
  REPORT_RESOLVED, // Report you submitted was resolved

  // System notifications
  BADGE_EARNED, // You earned a new badge
  MILESTONE_REACHED, // Completed activities milestone

  // General
  GENERAL // General notification
}
