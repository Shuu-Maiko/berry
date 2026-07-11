package com.shuu.berry.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "job_response_log")
public class JobResponseLog {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "secure_job_id", nullable = false)
  private String secureJobId;

  @Column(name = "http_status", nullable = false)
  private Integer httpStatus;

  @Column(name = "response_body", length = 2000)
  private String responseBody;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;
}
