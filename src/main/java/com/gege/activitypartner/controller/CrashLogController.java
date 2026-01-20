package com.gege.activitypartner.controller;

import com.gege.activitypartner.entity.CrashLog;
import com.gege.activitypartner.repository.CrashLogRepository;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/crash-logs")
@RequiredArgsConstructor
public class CrashLogController {

  private final CrashLogRepository crashLogRepository;

  @PostMapping
  public ResponseEntity<?> submitCrashLog(@RequestBody Map<String, Object> request) {
    try {
      CrashLog log = new CrashLog();
      log.setAppVersion((String) request.get("appVersion"));
      log.setOsVersion((String) request.get("osVersion"));
      log.setDeviceModel((String) request.get("deviceModel"));
      log.setErrorMessage((String) request.get("errorMessage"));
      log.setStackTrace((String) request.get("stackTrace"));
      log.setPlatform((String) request.get("platform"));

      if (request.get("userId") != null) {
        log.setUserId(((Number) request.get("userId")).longValue());
      }

      if (request.get("crashedAt") != null) {
        log.setCrashedAt(LocalDateTime.parse((String) request.get("crashedAt")));
      } else {
        log.setCrashedAt(LocalDateTime.now());
      }

      crashLogRepository.save(log);
      return ResponseEntity.ok(Map.of("success", true));
    } catch (Exception e) {
      return ResponseEntity.ok(Map.of("success", false));
    }
  }
}
