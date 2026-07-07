package com.shuu.berry.dto;

import com.shuu.berry.entity.JobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationMessage implements Serializable {
  private String secureJobId;
  private JobStatus status;
  private String message;
}
