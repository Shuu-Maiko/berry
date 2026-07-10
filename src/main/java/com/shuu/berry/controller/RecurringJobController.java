package com.shuu.berry.controller;

import com.shuu.berry.dto.JobRequestDTO;
import com.shuu.berry.dto.JobDetailsResponseDTO;
import com.shuu.berry.dto.JobRunHistoryDTO;
import com.shuu.berry.dto.JobSettingsUpdateDTO;
import com.shuu.berry.entity.Job;
import com.shuu.berry.entity.User;
import com.shuu.berry.service.JobService;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Data
@RestController
@RequestMapping("/api/jobs")
public class RecurringJobController {

  @Autowired
  private JobService jobService;

  @PostMapping("/create")
  public ResponseEntity<?> addJob(@Valid @RequestBody JobRequestDTO req) {
    try {
      User user = jobService.getAuthenticatedUser();
      Job job = jobService.createJob(req, user);

      return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
          "message", "Successfully scheduled Job",
          "jobName", job.getName(),
          "secureJobId", job.getSecureJobId(),
          "cronExpression", job.getCronExp()));
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
    }
  }

  @GetMapping
  public ResponseEntity<?> getMyJobs() {
    try {
      User user = jobService.getAuthenticatedUser();
      List<Job> jobs = jobService.getUserJobs(user);
      List<JobDetailsResponseDTO> detailedJobs = jobs.stream()
          .map(job -> jobService.getJobDetails(job.getSecureJobId(), user))
          .toList();
      return ResponseEntity.ok(detailedJobs);
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
    }
  }

  @DeleteMapping("/{secureJobId}")
  public ResponseEntity<?> deleteJob(@PathVariable String secureJobId) {
    try {
      User user = jobService.getAuthenticatedUser();
      jobService.deleteJob(secureJobId, user);
      return ResponseEntity.ok(Map.of("message", "Successfully deleted job: " + secureJobId));
    } catch (SecurityException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
    }
  }

  @GetMapping("/{secureJobId}/details")
  public ResponseEntity<?> getJobDetails(@PathVariable String secureJobId) {
    try {
      User user = jobService.getAuthenticatedUser();
      JobDetailsResponseDTO details = jobService.getJobDetails(secureJobId, user);
      return ResponseEntity.ok(details);
    } catch (SecurityException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
    }
  }

  @GetMapping("/{secureJobId}/history")
  public ResponseEntity<?> getJobHistory(@PathVariable String secureJobId) {
    try {
      User user = jobService.getAuthenticatedUser();
      List<JobRunHistoryDTO> history = jobService.getJobHistory(secureJobId, user);
      return ResponseEntity.ok(history);
    } catch (SecurityException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
    }
  }

  @PatchMapping("/{secureJobId}/settings")
  public ResponseEntity<?> updateJobSettings(
      @PathVariable String secureJobId,
      @RequestBody JobSettingsUpdateDTO req) {
    try {
      User user = jobService.getAuthenticatedUser();
      jobService.updateJobSettings(secureJobId, req.notifyOnFailure(), req.notifyOnSuccess(), user);
      return ResponseEntity.ok(Map.of("message", "Successfully updated job settings"));
    } catch (SecurityException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
    }
  }
}
