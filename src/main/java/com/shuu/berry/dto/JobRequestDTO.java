package com.shuu.berry.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.shuu.berry.entity.JobType;

import com.shuu.berry.entity.WebhookHttpMethod;

@Data
public class JobRequestDTO {
  @NotBlank(message = "Job name is required")
  @Size(max = 100, message = "Job name cannot exceed 100 characters")
  private String name;

  @NotBlank(message = "Cron expression is required")
  @Size(max = 100, message = "Cron expression cannot exceed 100 characters")
  private String cronString;

  @Size(max = 500, message = "Message cannot exceed 500 characters")
  private String message;

  @Size(max = 1000, message = "URL cannot exceed 1000 characters")
  private String url;

  @NotNull(message = "Job type is required")
  private JobType jobType;

  private WebhookHttpMethod httpMethod;
  private java.util.Map<String, String> httpHeaders;

  @Size(max = 2000, message = "Payload cannot exceed 2000 characters")
  private String payload;

  private Boolean notifyOnFailure;
  private Boolean notifyOnSuccess;
}
