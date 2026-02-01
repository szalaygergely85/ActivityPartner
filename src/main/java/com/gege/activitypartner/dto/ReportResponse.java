package com.gege.activitypartner.dto;

import com.gege.activitypartner.entity.ReportStatus;
import com.gege.activitypartner.entity.ReportType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
  private Long id;
  private Long reporterId;
  private String reporterName;
  private ReportType reportType;
  private Long reportedItemId;
  private Long reportedUserId; // For USER reports
  private Long reportedActivityId; // For ACTIVITY reports
  private Long reportedMessageId; // For MESSAGE reports
  private String reportedItemDescription;
  private String reason;
  private ReportStatus status;
  private LocalDateTime createdAt;
  private LocalDateTime resolvedAt;
}
