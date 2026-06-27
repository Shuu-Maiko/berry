package com.shuu.berry.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.shuu.berry.entity.JobType;

import com.shuu.berry.entity.WebhookHttpMethod;

@Data
public class JobRequestDTO {
  @NotBlank(message = "Job name is required")
  private String name;

  @NotBlank(message = "Cron expression is required")
  private String cronString;

  private String message;

  private String url;

  @NotNull(message = "Job type is required")
  private JobType jobType;

  private WebhookHttpMethod httpMethod;
  private java.util.Map<String, String> httpHeaders;
  private String payload;
}
