package com.gege.activitypartner.service;

import com.gege.activitypartner.config.JwtUtil;
import com.gege.activitypartner.entity.AccountDeletionRequest;
import com.gege.activitypartner.entity.Admin;
import com.gege.activitypartner.repository.AccountDeletionRequestRepository;
import com.gege.activitypartner.repository.AdminRepository;
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
