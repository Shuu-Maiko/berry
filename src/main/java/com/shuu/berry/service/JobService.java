package com.shuu.berry.service;

import com.shuu.berry.dto.JobRequestDTO;
import com.shuu.berry.dto.JobDetailsResponseDTO;
import com.shuu.berry.dto.JobRunHistoryDTO;
import com.shuu.berry.entity.Job;
import com.shuu.berry.entity.JobType;
import com.shuu.berry.entity.User;
import com.shuu.berry.repository.JobRepository;
import com.shuu.berry.repository.UserRepository;
import com.shuu.berry.utils.UuidUtil;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.storage.StorageProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class JobService {

  @Autowired
  private JobScheduler jobScheduler;
  @Autowired
  private PrintService printService;
  @Autowired
  private JobRepository jobRepository;
  @Autowired
  private WebhookService webhookService;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private JdbcTemplate jdbcTemplate;
  @Autowired
  private StorageProvider storageProvider;

  public User getAuthenticatedUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) {
      throw new RuntimeException("Unauthorized access");
    }
    String email = auth.getName();
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("Authenticated user not found in database"));
  }

  public Job createJob(JobRequestDTO req, User user) {
    if (req.getJobType() == JobType.WEBHOOK) {
      if (req.getUrl() == null || req.getUrl().trim().isEmpty()) {
        throw new IllegalArgumentException("URL is required for WEBHOOK job type");
      }
    }

    try {
      CronExpression.parse(req.getCronString());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid cron expression");
    }

    String secureJobId = UuidUtil.generateUuidV7().toString();

    Job job = Job.builder()
        .secureJobId(secureJobId)
        .name(req.getName())
        .cronExp(req.getCronString())
        .url(req.getUrl() != null ? req.getUrl() : "")
        .httpMethod(req.getHttpMethod())
        .httpHeaders(req.getHttpHeaders())
        .payload(req.getPayload())
        .jobType(req.getJobType() != null ? req.getJobType() : JobType.PRINT_LOG)
        .message(req.getMessage() != null ? req.getMessage() : "")
        .start(LocalDateTime.now())
        .user(user)
        .build();

    jobRepository.save(job);
    // TODO: maybe in future turn this all into strategy Pattern
    if (job.getJobType() == JobType.PRINT_LOG) {
      jobScheduler.scheduleRecurrently(
          secureJobId,
          req.getCronString(),
          () -> printService.printMessage(secureJobId, req.getMessage() != null ? req.getMessage() : ""));
    } else if (job.getJobType() == JobType.WEBHOOK) {
      jobScheduler.scheduleRecurrently(
          secureJobId,
          req.getCronString(),
          () -> webhookService.sendRequest(secureJobId, job.getUrl(), job.getHttpMethod(), job.getHttpHeaders(),
              job.getPayload()));
    }

    return job;
  }

  public List<Job> getUserJobs(User user) {
    return jobRepository.findByUser(user);
  }

  public void deleteJob(String secureJobId, User user) {
    Job job = jobRepository.findBySecureJobId(secureJobId)
        .orElseThrow(() -> new IllegalArgumentException("Job not found"));

    if (!job.getUser().getId().equals(user.getId())) {
      throw new SecurityException("You do not own this job");
    }

    jobRepository.delete(job);
    jobScheduler.deleteRecurringJob(secureJobId);
  }

  public JobDetailsResponseDTO getJobDetails(String secureJobId, User user) {
    Job job = jobRepository.findBySecureJobId(secureJobId)
        .orElseThrow(() -> new IllegalArgumentException("Job not found"));

    if (!job.getUser().getId().equals(user.getId())) {
      throw new SecurityException("You do not own this job");
    }

    java.time.Instant nextRunTime = null;
    try {
      CronExpression cronExpression = CronExpression.parse(job.getCronExp());
      LocalDateTime nextLocal = cronExpression.next(LocalDateTime.now());
      if (nextLocal != null) {
          nextRunTime = nextLocal.atZone(java.time.ZoneId.systemDefault()).toInstant();
      }
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }

    String historySql = "SELECT id, state, createdat, updatedat FROM jobrunr_jobs " +
        "WHERE recurringjobid = ? ORDER BY createdat DESC LIMIT 1";

    List<JobRunHistoryDTO> history = jdbcTemplate.query(historySql, (rs, rowNum) -> {
      java.util.Calendar utc = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));
      java.time.Instant createdAt = rs.getTimestamp("createdat", utc).toInstant();
      java.time.Instant updatedAt = rs.getTimestamp("updatedat", utc).toInstant();
      // TODO: in future to replace this with actual running time
      long durationMs = java.time.Duration.between(createdAt, updatedAt).toMillis();

      return new JobRunHistoryDTO(
          rs.getString("id").trim(),
          rs.getString("state"),
          createdAt,
          updatedAt,
          durationMs);
    }, secureJobId);

    JobRunHistoryDTO lastRun = null;
    java.time.Instant lastRunTime = null;
    String lastRunStatus = null;
    if (!history.isEmpty()) {
      lastRun = history.get(0);
      lastRunTime = lastRun.getCreatedAt();
      lastRunStatus = lastRun.getState();
    }

    boolean existsInJobRunr = storageProvider.getRecurringJobs().stream()
        .anyMatch(rj -> rj.getId().equals(secureJobId));
    String jobStatus = existsInJobRunr ? "ACTIVE" : "INACTIVE";

    return JobDetailsResponseDTO.builder()
        .id(job.getSecureJobId())
        .name(job.getName())
        .cronExpression(job.getCronExp())
        .jobType(job.getJobType().name())
        .message(job.getMessage())
        .url(job.getUrl())
        .httpMethod(job.getHttpMethod() != null ? job.getHttpMethod().name() : null)
        .httpHeaders(job.getHttpHeaders())
        .payload(job.getPayload())
        .createdAt(job.getStart() != null ? job.getStart().atZone(java.time.ZoneId.systemDefault()).toInstant() : null)
        .status(jobStatus)
        .lastRunTime(lastRunTime)
        .lastRunStatus(lastRunStatus)
        .nextRunTime(nextRunTime)
        .lastRun(lastRun)
        .build();
  }

  public List<JobRunHistoryDTO> getJobHistory(String secureJobId, User user) {
    Job job = jobRepository.findBySecureJobId(secureJobId)
        .orElseThrow(() -> new IllegalArgumentException("Job not found"));

    if (!job.getUser().getId().equals(user.getId())) {
      throw new SecurityException("You do not own this job");
    }

    String historySql = "SELECT id, state, createdat, updatedat FROM jobrunr_jobs " +
        "WHERE recurringjobid = ? ORDER BY createdat DESC LIMIT 100";

    return jdbcTemplate.query(historySql, (rs, rowNum) -> {
      java.util.Calendar utc = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));
      java.time.Instant createdAt = rs.getTimestamp("createdat", utc).toInstant();
      java.time.Instant updatedAt = rs.getTimestamp("updatedat", utc).toInstant();

      long durationMs = java.time.Duration.between(createdAt, updatedAt).toMillis();

      return new JobRunHistoryDTO(
          rs.getString("id").trim(),
          rs.getString("state"),
          createdAt,
          updatedAt,
          durationMs);
    }, secureJobId);
  }
}
