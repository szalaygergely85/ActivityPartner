package com.gege.activitypartner.repository;

import com.gege.activitypartner.entity.AppLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppLogRepository extends JpaRepository<AppLog, Long> {

  List<AppLog> findTop100ByOrderByReceivedAtDesc();

  List<AppLog> findTop100ByLevelOrderByReceivedAtDesc(String level);

  List<AppLog> findTop100ByTagOrderByReceivedAtDesc(String tag);

  List<AppLog> findByUserIdOrderByReceivedAtDesc(Long userId);
}
