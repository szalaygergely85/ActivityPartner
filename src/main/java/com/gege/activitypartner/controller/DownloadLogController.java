package com.gege.activitypartner.controller;

import com.gege.activitypartner.entity.DownloadLog;
import com.gege.activitypartner.repository.DownloadLogRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/downloads")
@RequiredArgsConstructor
public class DownloadLogController {

  private final DownloadLogRepository downloadLogRepository;

  @PostMapping
  public ResponseEntity<?> logDownload(@RequestBody Map<String, Object> request) {
    try {
      DownloadLog log = new DownloadLog();
      log.setAppVersion((String) request.get("appVersion"));
      log.setOsVersion((String) request.get("osVersion"));
      log.setDeviceModel((String) request.get("deviceModel"));
      log.setPlatform((String) request.get("platform"));
      log.setCountry((String) request.get("country"));
      log.setLanguage((String) request.get("language"));

      downloadLogRepository.save(log);
      return ResponseEntity.ok(Map.of("success", true));
    } catch (Exception e) {
      return ResponseEntity.ok(Map.of("success", false));
    }
  }
}
