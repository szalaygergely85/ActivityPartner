package com.gege.activitypartner.dto;

import com.gege.activitypartner.entity.ReportType;
import lombok.Data;

@Data
public class ReportRequest {
    private ReportType reportType;
    private Long reportedActivityId;
    private Long reportedMessageId;
    private Long reportedUserId;
    private String reason;
}
