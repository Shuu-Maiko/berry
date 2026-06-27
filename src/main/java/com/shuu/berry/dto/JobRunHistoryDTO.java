package com.shuu.berry.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobRunHistoryDTO {
  private String runId;
  private String state;
  private Instant createdAt;
  private Instant updatedAt;
  private Long durationMs;
}
