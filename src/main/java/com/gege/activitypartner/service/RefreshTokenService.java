package com.gege.activitypartner.service;

import com.gege.activitypartner.config.JwtUtil;
import com.gege.activitypartner.entity.RefreshToken;
import com.gege.activitypartner.entity.User;
import com.gege.activitypartner.exception.ResourceNotFoundException;
import com.gege.activitypartner.repository.RefreshTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtUtil jwtUtil;

  @Value("${jwt.refresh-expiration}")
  private Long refreshExpiration;

  // Create and save refresh token
  @Transactional
  public RefreshToken createRefreshToken(User user, HttpServletRequest request) {
    // Generate refresh token string
    String tokenString = jwtUtil.generateRefreshToken(user.getEmail(), user.getId());

    // Create refresh token entity
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setToken(tokenString);
    refreshToken.setUser(user);
    refreshToken.setExpiryDate(LocalDateTime.now().plusSeconds(refreshExpiration / 1000));
    refreshToken.setIsActive(true);

    // Extract device and IP information from request
    if (request != null) {
      refreshToken.setDeviceInfo(extractDeviceInfo(request));
      refreshToken.setIpAddress(extractIpAddress(request));
    }

    return refreshTokenRepository.save(refreshToken);
  }

  // Find refresh token by token string
  public RefreshToken findByToken(String token) {
    return refreshTokenRepository
        .findByToken(token)
        .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found"));
  }

  // Verify if refresh token is valid
  public RefreshToken verifyExpiration(RefreshToken token) {
    if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
      refreshTokenRepository.delete(token);
      throw new RuntimeException("Refresh token expired. Please login again.");
    }

    if (!token.getIsActive()) {
      throw new RuntimeException("Refresh token is no longer active. Please login again.");
    }

    // Update last used time
    token.setLastUsedAt(LocalDateTime.now());
    refreshTokenRepository.save(token);

    return token;
  }

  // Deactivate specific refresh token (logout from one device)
  @Transactional
  public void deactivateToken(String token) {
    refreshTokenRepository.deactivateToken(token);
  }

  // Deactivate all user tokens (logout from all devices)
  @Transactional
  public void deactivateAllUserTokens(Long userId) {
    refreshTokenRepository.deactivateAllUserTokens(userId);
  }

  // Get all active tokens for a user
  public List<RefreshToken> getActiveTokensForUser(User user) {
    return refreshTokenRepository.findByUserAndIsActiveTrue(user);
  }

  // Delete expired tokens (scheduled task)
  @Transactional
  public void deleteExpiredTokens() {
    refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
  }

  // Extract device information from request
  private String extractDeviceInfo(HttpServletRequest request) {
    String userAgent = request.getHeader("User-Agent");
    if (userAgent == null) {
      return "Unknown Device";
    }

    // Simple device detection
    if (userAgent.contains("Mobile")) {
      return "Mobile Device";
    } else if (userAgent.contains("Tablet")) {
      return "Tablet";
    } else {
      return "Desktop/Laptop";
    }
  }

  // Extract IP address from request
  private String extractIpAddress(HttpServletRequest request) {
    String ipAddress = request.getHeader("X-Forwarded-For");
    if (ipAddress == null || ipAddress.isEmpty()) {
      ipAddress = request.getRemoteAddr();
    }
    return ipAddress;
  }
}
