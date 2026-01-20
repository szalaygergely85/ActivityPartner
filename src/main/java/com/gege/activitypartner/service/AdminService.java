package com.gege.activitypartner.service;

import com.gege.activitypartner.config.JwtUtil;
import com.gege.activitypartner.entity.AccountDeletionRequest;
import com.gege.activitypartner.entity.Admin;
import com.gege.activitypartner.entity.CrashLog;
import com.gege.activitypartner.entity.DownloadLog;
import com.gege.activitypartner.entity.AppLog;
import com.gege.activitypartner.repository.AccountDeletionRequestRepository;
import com.gege.activitypartner.repository.AppLogRepository;
import com.gege.activitypartner.repository.AdminRepository;
import com.gege.activitypartner.repository.CrashLogRepository;
import com.gege.activitypartner.repository.DownloadLogRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

  private final AdminRepository adminRepository;
  private final AccountDeletionRequestRepository accountDeletionRequestRepository;
  private final CrashLogRepository crashLogRepository;
  private final DownloadLogRepository downloadLogRepository;
  private final AppLogRepository appLogRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;

  // Admin login
  @Transactional
  public String login(String username, String password) {
    Admin admin =
        adminRepository
            .findByUsername(username)
            .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

    if (!passwordEncoder.matches(password, admin.getPassword())) {
      throw new BadCredentialsException("Invalid credentials");
    }

    admin.setLastLoginAt(LocalDateTime.now());
    adminRepository.save(admin);

    return jwtUtil.generateAdminToken(admin.getUsername(), admin.getId());
  }

  // Validate admin token and return admin ID
  public Long validateAdminToken(String token) {
    try {
      String role = jwtUtil.extractRole(token);
      if (!"admin".equals(role)) {
        return null;
      }
      return jwtUtil.extractAdminId(token);
    } catch (Exception e) {
      return null;
    }
  }

  // Get all pending deletion requests
  public List<AccountDeletionRequest> getPendingDeletionRequests() {
    return accountDeletionRequestRepository.findByProcessedAtIsNullOrderByRequestedAtAsc();
  }

  // Mark deletion request as processed
  @Transactional
  public void markDeletionRequestProcessed(Long requestId) {
    accountDeletionRequestRepository
        .findById(requestId)
        .ifPresent(
            request -> {
              request.setProcessedAt(LocalDateTime.now());
              accountDeletionRequestRepository.save(request);
            });
  }

  // Get recent crash logs
  public List<CrashLog> getRecentCrashLogs() {
    return crashLogRepository.findTop100ByOrderByReceivedAtDesc();
  }

  // Get recent download logs
  public List<DownloadLog> getRecentDownloadLogs() {
    return downloadLogRepository.findTop100ByOrderByDownloadedAtDesc();
  }

  // Get download stats
  public DownloadStats getDownloadStats() {
    long total = downloadLogRepository.countTotal();
    long android = downloadLogRepository.countByPlatform("android");
    long ios = downloadLogRepository.countByPlatform("ios");
    return new DownloadStats(total, android, ios);
  }

  public record DownloadStats(long total, long android, long ios) {}

  // Get recent app logs
  public List<AppLog> getRecentAppLogs() {
    return appLogRepository.findTop100ByOrderByReceivedAtDesc();
  }

  // Create admin (for initial setup - call manually or via initializer)
  @Transactional
  public Admin createAdmin(String username, String password) {
    if (adminRepository.existsByUsername(username)) {
      throw new IllegalArgumentException("Admin username already exists");
    }

    Admin admin = new Admin();
    admin.setUsername(username);
    admin.setPassword(passwordEncoder.encode(password));
    return adminRepository.save(admin);
  }
}
