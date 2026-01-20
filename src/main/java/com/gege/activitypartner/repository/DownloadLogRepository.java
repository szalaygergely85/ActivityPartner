package com.gege.activitypartner.repository;

import com.gege.activitypartner.entity.DownloadLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DownloadLogRepository extends JpaRepository<DownloadLog, Long> {

  List<DownloadLog> findTop100ByOrderByDownloadedAtDesc();

  long countByPlatform(String platform);

  @Query("SELECT COUNT(d) FROM DownloadLog d")
  long countTotal();
}
