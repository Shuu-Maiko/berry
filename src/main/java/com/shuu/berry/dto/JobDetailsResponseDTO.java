package com.shuu.berry.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobDetailsResponseDTO {
  private String id;
  private String name;
  private String cronExpression;
  private String jobType;
  private String message;
  private String url;

  private String httpMethod;
  private java.util.Map<String, String> httpHeaders;
  private String payload;

  private Instant createdAt;
  private String status;

  private Instant lastRunTime;
  private String lastRunStatus;
  private Instant nextRunTime;

  private boolean notifyOnFailure;
  private boolean notifyOnSuccess;

  private JobRunHistoryDTO lastRun;
}
