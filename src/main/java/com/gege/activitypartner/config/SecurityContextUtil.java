package com.gege.activitypartner.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityContextUtil {

  private final JwtUtil jwtUtil;
  private final HttpServletRequest request;

  /**
   * Get the email of the currently authenticated user
   *
   * @return email of authenticated user
   * @throws RuntimeException if no user is authenticated
   */
  public String getCurrentUserEmail() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      throw new RuntimeException("No authenticated user found");
    }

    Object principal = authentication.getPrincipal();

    if (principal instanceof UserDetails) {
      return ((UserDetails) principal).getUsername();
    } else {
      return principal.toString();
    }
  }

  /**
   * Get the user ID of the currently authenticated user from JWT token This avoids database lookup
   * by extracting from JWT claims directly
   *
   * @return user ID of authenticated user
   * @throws RuntimeException if no user is authenticated or userId cannot be extracted
   */
  public Long getCurrentUserId() {
    try {
      // Extract JWT token from Authorization header
      final String authHeader = request.getHeader("Authorization");

      if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        throw new RuntimeException("No valid Authorization header found");
      }

      final String jwt = authHeader.substring(7);
      Long userId = jwtUtil.extractUserId(jwt);

      if (userId == null) {
        throw new RuntimeException("No userId found in JWT token");
      }

      return userId;
    } catch (Exception e) {
      throw new RuntimeException("Failed to extract user ID from JWT: " + e.getMessage());
    }
  }

  /**
   * Check if there is an authenticated user in the context
   *
   * @return true if user is authenticated, false otherwise
   */
  public boolean isAuthenticated() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication != null
        && authentication.isAuthenticated()
        && !(authentication.getPrincipal() instanceof String
            && authentication.getPrincipal().equals("anonymousUser"));
  }
}
