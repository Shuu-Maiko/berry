package com.shuu.berry.config;

import com.shuu.berry.dto.NotificationMessage;
import com.shuu.berry.entity.JobStatus;
import lombok.RequiredArgsConstructor;
import org.jobrunr.jobs.Job;
import org.jobrunr.jobs.filters.ApplyStateFilter;
import org.jobrunr.jobs.states.JobState;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JobRunrNotificationFilter implements ApplyStateFilter {

  private final RabbitTemplate rabbitTemplate;

  @Override
  public void onStateApplied(Job job, JobState oldState, JobState newState) {
    JobStatus status = JobStatus.fromStateName(newState.getName().name());
    if (status == null)
      return;

    NotificationMessage message = NotificationMessage.builder()
        .secureJobId(job.getId().toString())
        .status(status)
        .message(job.getJobName() + " finished with state: " + status)
        .build();

    rabbitTemplate.convertAndSend(
        RabbitMQConfig.NOTIFICATION_EXCHANGE,
        RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
        message);
  }
}
