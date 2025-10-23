package com.gege.activitypartner.entity;

public enum ParticipantStatus {
    INTERESTED,  // User expressed interest, awaiting creator's acceptance
    ACCEPTED,    // Creator accepted the user (awaiting user confirmation)
    DECLINED,    // Creator declined the user's interest
    JOINED,      // User confirmed participation after being accepted
    LEFT,        // User left the activity after joining
    WITHDRAWN    // User withdrew interest/acceptance before joining
}
