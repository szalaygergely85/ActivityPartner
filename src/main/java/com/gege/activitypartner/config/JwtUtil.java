package com.gege.activitypartner.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.expiration}")
  private Long expiration;

  @Value("${jwt.refresh-expiration}")
  private Long refreshExpiration;

  // Generate secret key from the secret string
  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  // Extract username from token
  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  // Extract user ID from token
  public Long extractUserId(String token) {
    return extractClaim(token, claims -> claims.get("userId", Long.class));
  }

  // Extract expiration date from token
  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  // Extract a specific claim from token
  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  // Extract all claims from token
  private Claims extractAllClaims(String token) {
    return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
  }

  // Check if token is expired
  private Boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  // Generate access token for user
  public String generateToken(String email, Long userId) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", userId);
    return createToken(claims, email, expiration);
  }

  // Generate refresh token for user
  public String generateRefreshToken(String email, Long userId) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", userId);
    claims.put("type", "refresh");
    return createToken(claims, email, refreshExpiration);
  }

  // Create JWT token with custom expiration
  private String createToken(Map<String, Object> claims, String subject, Long expirationTime) {
    return Jwts.builder()
        .setClaims(claims)
        .setSubject(subject)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  // Validate token
  public Boolean validateToken(String token, String email) {
    final String username = extractUsername(token);
    return (username.equals(email) && !isTokenExpired(token));
  }
}
