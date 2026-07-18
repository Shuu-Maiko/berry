package com.shuu.berry.controller;

import com.shuu.berry.service.AuthService;
import com.shuu.berry.dto.LoginRequestDTO;
import com.shuu.berry.dto.SignupRequestDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  @Value("${app.env:prod}")
  private String appEnv;

  @Autowired
  private AuthService authService;

  @PostMapping("/signup")
  public ResponseEntity<?> signup(@Valid @RequestBody SignupRequestDTO req) {
    try {
      authService.registerUser(req);
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(Map.of("message", "User registered successfully"));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(Map.of("message", e.getMessage()));
    }
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO req) {
    try {
      String token = authService.authenticateUser(req);
      boolean isSecure = !"dev".equalsIgnoreCase(appEnv);

      org.springframework.http.ResponseCookie cookie = org.springframework.http.ResponseCookie
          .from("auth_token", token)
          .httpOnly(true)
          .secure(isSecure) // NOTE: false for dev, true for prod
          .sameSite(isSecure ? "None" : "Lax")
          .path("/")
          .maxAge(86400) // 1 day
          .build();

      return ResponseEntity.ok()
          .header(org.springframework.http.HttpHeaders.SET_COOKIE, cookie.toString())
          .body(Map.of("message", "Login successful"));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("message", e.getMessage()));
    }
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logout() {
    boolean isSecure = !"dev".equalsIgnoreCase(appEnv);

    org.springframework.http.ResponseCookie cookie = org.springframework.http.ResponseCookie.from("auth_token", "")
        .httpOnly(true)
        .secure(isSecure)
        .sameSite(isSecure ? "None" : "Lax")
        .path("/")
        .maxAge(0)
        .build();

    return ResponseEntity.ok()
        .header(org.springframework.http.HttpHeaders.SET_COOKIE, cookie.toString())
        .body(Map.of("message", "Logged out successfully"));
  }
}
