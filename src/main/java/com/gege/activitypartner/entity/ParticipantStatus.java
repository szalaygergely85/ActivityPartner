package com.gege.activitypartner.entity;

public enum ParticipantStatus {
  INTERESTED, // User expressed interest, awaiting creator's acceptance
  ACCEPTED, // Creator accepted the user — fully joined, no further confirmation needed
  DECLINED, // Creator declined the user's interest
  JOINED, // Legacy — kept for backward compatibility with existing DB records
  LEFT, // User left the activity after being accepted
  WITHDRAWN // User withdrew interest before being accepted
}
