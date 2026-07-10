package com.shuu.berry.controller;

import com.shuu.berry.dto.NotificationChannelRequestDTO;
import com.shuu.berry.dto.NotificationChannelResponseDTO;
import com.shuu.berry.entity.User;
import com.shuu.berry.service.JobService;
import com.shuu.berry.service.NotificationChannelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications/channels")
@RequiredArgsConstructor
public class NotificationChannelController {

  private final JobService jobService;
  private final NotificationChannelService channelService;

  @GetMapping
  public List<NotificationChannelResponseDTO> getMyChannels() {
    User user = jobService.getAuthenticatedUser();
    return channelService.getUserChannels(user);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public NotificationChannelResponseDTO addChannel(@Valid @RequestBody NotificationChannelRequestDTO req) {
    User user = jobService.getAuthenticatedUser();
    return channelService.addChannel(user, req);
  }

  @DeleteMapping("/{id}")
  public Map<String, String> deleteChannel(@PathVariable long id) {
    User user = jobService.getAuthenticatedUser();
    channelService.deleteChannel(user, id);
    return Map.of("message", "Successfully deleted notification channel: " + id);
  }

  @ExceptionHandler(SecurityException.class)
  public ResponseEntity<Map<String, String>> handleSecurity(SecurityException e) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException e) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
  }
}
