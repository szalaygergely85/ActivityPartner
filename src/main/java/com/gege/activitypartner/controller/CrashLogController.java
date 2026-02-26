package com.gege.activitypartner.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
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

  private final ObjectMapper objectMapper;

  @PostMapping
  public ResponseEntity<?> submitCrashLog(@RequestBody Map<String, Object> request) {
    try {
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
