package com.shuu.berry.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.time.LocalDateTime;

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
  private LocalDateTime createdAt;
  private String status;

  private Instant lastRunTime;
  private String lastRunStatus;
  private LocalDateTime nextRunTime;

  private JobRunHistoryDTO lastRun;
}
