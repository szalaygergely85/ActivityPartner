package com.gege.activitypartner.repository;

import com.gege.activitypartner.entity.Report;
import com.gege.activitypartner.entity.ReportStatus;
import com.gege.activitypartner.entity.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByReporterIdOrderByCreatedAtDesc(Long reporterId);

    List<Report> findByStatusOrderByCreatedAtDesc(ReportStatus status);

    List<Report> findByReportTypeAndStatusOrderByCreatedAtDesc(ReportType reportType, ReportStatus status);

    @Query("SELECT COUNT(r) FROM Report r WHERE r.reportedActivity.id = :activityId")
    Long countByActivityId(@Param("activityId") Long activityId);

    @Query("SELECT COUNT(r) FROM Report r WHERE r.reportedMessage.id = :messageId")
    Long countByMessageId(@Param("messageId") Long messageId);

    @Query("SELECT r FROM Report r WHERE r.reportedActivity.id = :activityId AND r.reporter.id = :reporterId")
    List<Report> findByActivityIdAndReporterId(@Param("activityId") Long activityId, @Param("reporterId") Long reporterId);

    @Query("SELECT r FROM Report r WHERE r.reportedMessage.id = :messageId AND r.reporter.id = :reporterId")
    List<Report> findByMessageIdAndReporterId(@Param("messageId") Long messageId, @Param("reporterId") Long reporterId);
}
