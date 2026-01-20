package com.gege.activitypartner.repository;

import com.gege.activitypartner.entity.CrashLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CrashLogRepository extends JpaRepository<CrashLog, Long> {

  List<CrashLog> findTop100ByOrderByReceivedAtDesc();

  List<CrashLog> findByUserIdOrderByReceivedAtDesc(Long userId);

  List<CrashLog> findByPlatformOrderByReceivedAtDesc(String platform);
}
