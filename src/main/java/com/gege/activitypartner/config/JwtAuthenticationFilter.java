package com.gege.activitypartner.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final UserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    // Extract JWT token from Authorization header or cookie
    String jwt = null;

    // First, try Authorization header (for mobile apps)
    final String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      jwt = authHeader.substring(7);
    }

    // Fallback to cookie (for web app)
    if (jwt == null && request.getCookies() != null) {
      jwt =
          Arrays.stream(request.getCookies())
              .filter(cookie -> "accessToken".equals(cookie.getName()))
              .map(Cookie::getValue)
              .findFirst()
              .orElse(null);
    }

    // No token found, continue without authentication
    if (jwt == null) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      // Extract user email from token
      final String userEmail = jwtUtil.extractUsername(jwt);

      // If token contains email and user is not already authenticated
      if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        // Load user details
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

        // Validate token
        if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {
          // Create authentication token
          UsernamePasswordAuthenticationToken authToken =
              new UsernamePasswordAuthenticationToken(
                  userDetails, null, userDetails.getAuthorities());

          // Set authentication details
          authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

          // Set authentication in security context
          SecurityContextHolder.getContext().setAuthentication(authToken);
        }
      }
    } catch (Exception e) {
      // Log the error and continue without authentication
      logger.error("Cannot set user authentication: {}", e);
    }

    filterChain.doFilter(request, response);
  }
}
