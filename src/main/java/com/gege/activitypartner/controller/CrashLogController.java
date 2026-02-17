package com.gege.activitypartner.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gege.activitypartner.entity.CrashLog;
import com.gege.activitypartner.repository.CrashLogRepository;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/crash-logs")
@RequiredArgsConstructor
public class CrashLogController {

  private static final Logger log = LoggerFactory.getLogger(CrashLogController.class);
  private static final Path CRASH_LOG_DIR = Paths.get("logs/crashes");

  private final CrashLogRepository crashLogRepository;
  private final ObjectMapper objectMapper;

  @PostMapping
  public ResponseEntity<?> submitCrashLog(@RequestBody Map<String, Object> request) {
    try {
      CrashLog crashLog = new CrashLog();
      crashLog.setAppVersion((String) request.get("appVersion"));
      crashLog.setOsVersion((String) request.get("osVersion"));
      crashLog.setDeviceModel((String) request.get("deviceModel"));
      crashLog.setErrorMessage((String) request.get("errorMessage"));
      crashLog.setStackTrace((String) request.get("stackTrace"));
      crashLog.setPlatform((String) request.get("platform"));

      if (request.get("userId") != null) {
        crashLog.setUserId(((Number) request.get("userId")).longValue());
      }

      if (request.get("crashedAt") != null) {
        crashLog.setCrashedAt(LocalDateTime.parse((String) request.get("crashedAt")));
      } else {
        crashLog.setCrashedAt(LocalDateTime.now());
      }

      crashLogRepository.save(crashLog);
      writeCrashToFile(request);
      return ResponseEntity.ok(Map.of("success", true));
    } catch (Exception e) {
      log.error("Failed to save crash log", e);
      return ResponseEntity.ok(Map.of("success", false));
    }
  }

  private void writeCrashToFile(Map<String, Object> crashData) {
    try {
      Files.createDirectories(CRASH_LOG_DIR);
      String date = LocalDate.now().toString();
      Path file = CRASH_LOG_DIR.resolve("crashes-" + date + ".json");
      String line = objectMapper.writeValueAsString(crashData) + System.lineSeparator();
      Files.writeString(
          file, line, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    } catch (IOException e) {
      log.error("Failed to write crash log to file", e);
    }
  }
}
