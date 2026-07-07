package com.shuu.berry.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "job")
public class Job {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(name = "secure_job_id", unique = true, nullable = false)
  private String secureJobId;

  private String name;

  @Column(name = "corn_expression")
  private String cronExp;

  private String url;

  @Enumerated(EnumType.STRING)
  @Column(name = "job_type")
  private JobType jobType;

  @Enumerated(EnumType.STRING)
  @Column(name = "http_method")
  private WebhookHttpMethod httpMethod;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "http_headers", columnDefinition = "jsonb")
  private java.util.Map<String, String> httpHeaders;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  private String payload;

  private String message;

  @Column(name = "start_time")
  private LocalDateTime start;

  @Column(name = "end_time")
  private LocalDateTime end;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Builder.Default
  @Column(name = "notify_on_failure")
  private boolean notifyOnFailure = true;

  @Builder.Default
  @Column(name = "notify_on_success")
  private boolean notifyOnSuccess = false;
}
