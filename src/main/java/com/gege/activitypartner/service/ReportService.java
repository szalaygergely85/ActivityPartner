package com.gege.activitypartner.service;

import com.gege.activitypartner.dto.ReportRequest;
import com.gege.activitypartner.dto.ReportResponse;
import com.gege.activitypartner.entity.*;
import com.gege.activitypartner.exception.ResourceNotFoundException;
import com.gege.activitypartner.repository.*;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

  private final ReportRepository reportRepository;
  private final UserRepository userRepository;
  private final ActivityRepository activityRepository;
  private final ActivityMessageRepository messageRepository;
  private final NotificationService notificationService;

  /** Submit a new report */
  @Transactional
  public ReportResponse submitReport(Long reporterId, ReportRequest request) {
    // Validate reporter exists
    User reporter =
        userRepository
            .findById(reporterId)
            .orElseThrow(
                () -> new ResourceNotFoundException("User not found with id: " + reporterId));

    // Validate request
    validateReportRequest(request);

    // Check for duplicate reports
    if (isDuplicateReport(reporterId, request)) {
      throw new IllegalArgumentException("You have already reported this item");
    }

    // Create report
    Report report = new Report();
    report.setReporter(reporter);
    report.setReportType(request.getReportType());
    report.setReason(request.getReason());
    report.setStatus(ReportStatus.PENDING);

    // Set the reported item based on type
    switch (request.getReportType()) {
      case ACTIVITY:
        Activity activity =
            activityRepository
                .findById(request.getReportedActivityId())
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found"));
        report.setReportedActivity(activity);
        report.setReportedUser(activity.getCreator()); // Also track the activity creator
        break;

      case MESSAGE:
        ActivityMessage message =
            messageRepository
                .findById(request.getReportedMessageId())
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
        report.setReportedMessage(message);
        report.setReportedUser(message.getUser()); // Also track the message sender
        break;

      case USER:
        User reportedUser =
            userRepository
                .findById(request.getReportedUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        report.setReportedUser(reportedUser);
        break;

      default:
        throw new IllegalArgumentException("Invalid report type");
    }

    Report saved = reportRepository.save(report);

    // Notify reporter that report was submitted
    notificationService.createAndSendNotification(
        reporter,
        "Report Submitted",
        "Your report has been submitted and is being reviewed by our team.",
        NotificationType.REPORT_SUBMITTED,
        null,
        null,
        null);

    return mapToResponse(saved);
  }

  /** Get all reports submitted by a user */
  @Transactional(readOnly = true)
  public List<ReportResponse> getMyReports(Long userId) {
    userRepository
        .findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    return reportRepository.findByReporterIdOrderByCreatedAtDesc(userId).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  /** Get all pending reports (admin only) */
  @Transactional(readOnly = true)
  public List<ReportResponse> getPendingReports() {
    return reportRepository.findByStatusOrderByCreatedAtDesc(ReportStatus.PENDING).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  /** Get reports by type and status (admin only) */
  @Transactional(readOnly = true)
  public List<ReportResponse> getReportsByTypeAndStatus(ReportType type, ReportStatus status) {
    return reportRepository.findByReportTypeAndStatusOrderByCreatedAtDesc(type, status).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  /** Update report status (admin only) */
  @Transactional
  public ReportResponse updateReportStatus(Long reportId, ReportStatus newStatus) {
    Report report =
        reportRepository
            .findById(reportId)
            .orElseThrow(() -> new ResourceNotFoundException("Report not found"));

    ReportStatus oldStatus = report.getStatus();
    report.setStatus(newStatus);

    if (newStatus == ReportStatus.RESOLVED || newStatus == ReportStatus.DISMISSED) {
      report.setResolvedAt(java.time.LocalDateTime.now());
    }

    Report updated = reportRepository.save(report);

    // Notify reporter if status changed to resolved or dismissed
    if (newStatus == ReportStatus.RESOLVED || newStatus == ReportStatus.DISMISSED) {
      String statusText = newStatus == ReportStatus.RESOLVED ? "resolved" : "dismissed";
      notificationService.createAndSendNotification(
          report.getReporter(),
          "Report " + statusText,
          "Your report has been " + statusText + ". Thank you for helping keep our community safe.",
          NotificationType.REPORT_RESOLVED,
          null,
          null,
          null);
    }

    return mapToResponse(updated);
  }

  /** Get report count for a specific activity */
  @Transactional(readOnly = true)
  public Long getActivityReportCount(Long activityId) {
    return reportRepository.countByActivityId(activityId);
  }

  /** Get report count for a specific message */
  @Transactional(readOnly = true)
  public Long getMessageReportCount(Long messageId) {
    return reportRepository.countByMessageId(messageId);
  }

  /** Validate report request */
  private void validateReportRequest(ReportRequest request) {
    if (request.getReason() == null || request.getReason().trim().isEmpty()) {
      throw new IllegalArgumentException("Report reason cannot be empty");
    }

    if (request.getReportType() == null) {
      throw new IllegalArgumentException("Report type is required");
    }

    // Validate that the appropriate ID is provided
    switch (request.getReportType()) {
      case ACTIVITY:
        if (request.getReportedActivityId() == null) {
          throw new IllegalArgumentException("Activity ID is required for activity reports");
        }
        break;
      case MESSAGE:
        if (request.getReportedMessageId() == null) {
          throw new IllegalArgumentException("Message ID is required for message reports");
        }
        break;
      case USER:
        if (request.getReportedUserId() == null) {
          throw new IllegalArgumentException("User ID is required for user reports");
        }
        break;
    }
  }

  /** Check if user has already reported this item */
  private boolean isDuplicateReport(Long reporterId, ReportRequest request) {
    switch (request.getReportType()) {
      case ACTIVITY:
        return !reportRepository
            .findByActivityIdAndReporterId(request.getReportedActivityId(), reporterId)
            .isEmpty();
      case MESSAGE:
        return !reportRepository
            .findByMessageIdAndReporterId(request.getReportedMessageId(), reporterId)
            .isEmpty();
      case USER:
        // For user reports, we allow multiple reports (different reasons)
        return false;
      default:
        return false;
    }
  }

  /** Map Report entity to ReportResponse DTO */
  private ReportResponse mapToResponse(Report report) {
    ReportResponse response = new ReportResponse();
    response.setId(report.getId());
    response.setReporterId(report.getReporter().getId());
    response.setReporterName(report.getReporter().getFullName());
    response.setReportType(report.getReportType());
    response.setReason(report.getReason());
    response.setStatus(report.getStatus());
    response.setCreatedAt(report.getCreatedAt());
    response.setResolvedAt(report.getResolvedAt());

    // Set reported item details based on type
    switch (report.getReportType()) {
      case ACTIVITY:
        if (report.getReportedActivity() != null) {
          response.setReportedItemId(report.getReportedActivity().getId());
          response.setReportedItemDescription(report.getReportedActivity().getTitle());
        }
        break;
      case MESSAGE:
        if (report.getReportedMessage() != null) {
          response.setReportedItemId(report.getReportedMessage().getId());
          response.setReportedItemDescription(
              truncateText(report.getReportedMessage().getMessageText(), 50));
        }
        break;
      case USER:
        if (report.getReportedUser() != null) {
          response.setReportedItemId(report.getReportedUser().getId());
          response.setReportedItemDescription(report.getReportedUser().getFullName());
        }
        break;
    }

    return response;
  }

  /** Truncate text for display */
  private String truncateText(String text, int maxLength) {
    if (text.length() > maxLength) {
      return text.substring(0, maxLength - 3) + "...";
    }
    return text;
  }
}
