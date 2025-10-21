package com.gege.activitypartner.dto;

import com.gege.activitypartner.entity.ReportStatus;
import com.gege.activitypartner.entity.ReportType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
    private Long id;
    private Long reporterId;
    private String reporterName;
    private ReportType reportType;
    private Long reportedItemId;
    private String reportedItemDescription;
    private String reason;
    private ReportStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}
