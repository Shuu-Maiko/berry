package com.shuu.berry.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

import jakarta.annotation.PostConstruct;

@Component
public class JwtUtil {

  @Value("${jwt.secret:}")
  private String secret;

  @PostConstruct
  public void init() {
    if (secret == null || secret.isBlank()) {
      System.err.println("You MUST provide JWT_SECRET in your .env file or environment.");

      throw new IllegalStateException("Missing JWT_SECRET environment variable.");
    }
  }

  @Value("${jwt.expiration:86400000}") // 1 day
  private long expiration;

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  public String generateToken(String email) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + expiration);

    return Jwts.builder()
        .subject(email)
        .issuedAt(now)
        .expiration(expiryDate)
        .signWith(getSigningKey(), Jwts.SIG.HS256)
        .compact();
  }

  public String extractEmail(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  public boolean validateToken(String token, String email) {
    final String extractedEmail = extractEmail(token);
    return (extractedEmail.equals(email) && !isTokenExpired(token));
  }
}
