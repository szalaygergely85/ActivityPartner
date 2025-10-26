package com.gege.activitypartner.controller;

import com.gege.activitypartner.config.SecurityContextUtil;
import com.gege.activitypartner.dto.ReportRequest;
import com.gege.activitypartner.dto.ReportResponse;
import com.gege.activitypartner.entity.ReportStatus;
import com.gege.activitypartner.entity.ReportType;
import com.gege.activitypartner.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportService reportService;
    private final SecurityContextUtil securityContextUtil;

    /**
     * Submit a new report
     * POST /api/reports
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReportResponse> submitReport(@Valid @RequestBody ReportRequest request) {
        Long userId = securityContextUtil.getCurrentUserId();

        ReportResponse response = reportService.submitReport(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all reports submitted by current user
     * GET /api/reports/my-reports
     */
    @GetMapping("/my-reports")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReportResponse>> getMyReports() {
        Long userId = securityContextUtil.getCurrentUserId();

        List<ReportResponse> reports = reportService.getMyReports(userId);
        return ResponseEntity.ok(reports);
    }

    /**
     * Get all pending reports (admin only)
     * GET /api/reports/pending
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReportResponse>> getPendingReports() {
        List<ReportResponse> reports = reportService.getPendingReports();
        return ResponseEntity.ok(reports);
    }

    /**
     * Get reports by type and status (admin only)
     * GET /api/reports?type=ACTIVITY&status=PENDING
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReportResponse>> getReportsByTypeAndStatus(
            @RequestParam ReportType type,
            @RequestParam ReportStatus status) {

        List<ReportResponse> reports = reportService.getReportsByTypeAndStatus(type, status);
        return ResponseEntity.ok(reports);
    }

    /**
     * Update report status (admin only)
     * PATCH /api/reports/{reportId}/status
     */
    @PatchMapping("/{reportId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReportResponse> updateReportStatus(
            @PathVariable Long reportId,
            @RequestParam ReportStatus status) {

        ReportResponse response = reportService.updateReportStatus(reportId, status);
        return ResponseEntity.ok(response);
    }

    /**
     * Get report count for an activity
     * GET /api/reports/activity/{activityId}/count
     */
    @GetMapping("/activity/{activityId}/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Long>> getActivityReportCount(@PathVariable Long activityId) {
        Long count = reportService.getActivityReportCount(activityId);
        return ResponseEntity.ok(Map.of("reportCount", count));
    }

    /**
     * Get report count for a message
     * GET /api/reports/message/{messageId}/count
     */
    @GetMapping("/message/{messageId}/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Long>> getMessageReportCount(@PathVariable Long messageId) {
        Long count = reportService.getMessageReportCount(messageId);
        return ResponseEntity.ok(Map.of("reportCount", count));
    }
}
