package com.gege.activitypartner.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final UserDetailsService userDetailsService;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable()) // Disable CSRF for stateless JWT authentication
        .authorizeHttpRequests(
            auth ->
                auth
                    // Public endpoints - no authentication required
                    .requestMatchers(
                        "/api/users/register",
                        "/api/users/login",
                        "/api/users/refresh-token",
                        "/api/users/web-logout")
                    .permitAll()
                    .requestMatchers("/api/categories/**")
                    .permitAll() // Categories are public for browsing
                    .requestMatchers(
                        "/api/activities",
                        "/api/activities/trending",
                        "/api/activities/nearby",
                        "/api/activities/category/**",
                        "/api/activities/{id}")
                    .permitAll() // Activities are public for browsing
                    .requestMatchers("/api/covers/**")
                    .permitAll() // Cover images are public for activity creation
                    .requestMatchers("/", "/privacy", "/terms", "/support", "/delete-account", "/login", "/register")
                    .permitAll() // Static pages are public
                    .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico")
                    .permitAll() // Static resources are public
                    .requestMatchers("/api/users/request-deletion")
                    .permitAll() // Account deletion request is public (Google Play compliance)
                    .requestMatchers("/api/crash-logs")
                    .permitAll() // Crash logs can be sent without auth
                    .requestMatchers("/api/downloads")
                    .permitAll() // Download logs can be sent without auth
                    .requestMatchers("/api/logs", "/api/logs/**")
                    .permitAll() // App logs can be sent without auth
                    .requestMatchers("/admin/**")
                    .permitAll() // Admin endpoints handle their own auth via token

                    // All other endpoints require authentication
                    .anyRequest()
                    .authenticated())
        .sessionManagement(
            session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS) // Stateless sessions for JWT
            )
        .authenticationProvider(authenticationProvider())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
