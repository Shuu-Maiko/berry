package com.shuu.berry.config;

import com.shuu.berry.dto.NotificationMessage;
import com.shuu.berry.entity.JobStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.jobs.Job;
import org.jobrunr.jobs.filters.ApplyStateFilter;
import org.jobrunr.jobs.states.JobState;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobRunrNotificationFilter implements ApplyStateFilter {

  private final RabbitTemplate rabbitTemplate;

  @Override
  public void onStateApplied(Job job, JobState oldState, JobState newState) {
    log.info("Job {} state changed to {}", job.getId(), newState.getName());
    JobStatus status = JobStatus.fromStateName(newState.getName().name());
    if (status == null) {
      log.debug("State {} is not a monitored state, skipping notification", newState.getName());
      return;
    }
    NotificationMessage message = NotificationMessage.builder()
        .secureJobId(job.getRecurringJobId().orElse(job.getId().toString()))
        .status(status)
        .message(job.getJobName() + " finished with state: " + status)
        .build();

    log.info("Publishing notification event for job {} with status {}", job.getId(), status);
    rabbitTemplate.convertAndSend(
        RabbitMQConfig.NOTIFICATION_EXCHANGE,
        RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
        message);
  }
}
