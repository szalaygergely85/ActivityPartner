package com.gege.activitypartner.controller;

import com.gege.activitypartner.entity.AppLog;
import com.gege.activitypartner.repository.AppLogRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class AppLogController {

  private final AppLogRepository appLogRepository;

  @PostMapping
  public ResponseEntity<?> submitLog(@RequestBody Map<String, Object> request) {
    try {
      AppLog log = new AppLog();
      log.setLevel((String) request.get("level"));
      log.setTag((String) request.get("tag"));
      log.setMessage((String) request.get("message"));
      log.setAppVersion((String) request.get("appVersion"));
      log.setOsVersion((String) request.get("osVersion"));
      log.setDeviceModel((String) request.get("deviceModel"));
      log.setPlatform((String) request.get("platform"));

      if (request.get("userId") != null) {
        log.setUserId(((Number) request.get("userId")).longValue());
      }

      if (request.get("loggedAt") != null) {
        log.setLoggedAt(LocalDateTime.parse((String) request.get("loggedAt")));
      } else {
        log.setLoggedAt(LocalDateTime.now());
      }

      appLogRepository.save(log);
      return ResponseEntity.ok(Map.of("success", true));
    } catch (Exception e) {
      return ResponseEntity.ok(Map.of("success", false));
    }
  }

  // Batch submit multiple logs at once
  @PostMapping("/batch")
  public ResponseEntity<?> submitLogs(@RequestBody List<Map<String, Object>> logs) {
    try {
      for (Map<String, Object> request : logs) {
        AppLog log = new AppLog();
        log.setLevel((String) request.get("level"));
        log.setTag((String) request.get("tag"));
        log.setMessage((String) request.get("message"));
        log.setAppVersion((String) request.get("appVersion"));
        log.setOsVersion((String) request.get("osVersion"));
        log.setDeviceModel((String) request.get("deviceModel"));
        log.setPlatform((String) request.get("platform"));

        if (request.get("userId") != null) {
          log.setUserId(((Number) request.get("userId")).longValue());
        }

        if (request.get("loggedAt") != null) {
          log.setLoggedAt(LocalDateTime.parse((String) request.get("loggedAt")));
        } else {
          log.setLoggedAt(LocalDateTime.now());
        }

        appLogRepository.save(log);
      }
      return ResponseEntity.ok(Map.of("success", true, "count", logs.size()));
    } catch (Exception e) {
      return ResponseEntity.ok(Map.of("success", false));
    }
  }
}
