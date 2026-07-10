package com.shuu.berry.notification;

import com.shuu.berry.config.RabbitMQConfig;
import com.shuu.berry.dto.NotificationMessage;
import com.shuu.berry.entity.Job;
import com.shuu.berry.entity.NotificationChannel;
import com.shuu.berry.repository.JobRepository;
import com.shuu.berry.repository.NotificationChannelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationConsumer {

  private final JobRepository jobRepository;
  private final NotificationChannelRepository channelRepository;
  private final List<AbstractNotificationHandler> handlers;

  @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE, concurrency = "5-25")
  public void consumeMessage(NotificationMessage message) {
    log.info("Received notification message from queue: job={}, status={}", message.getSecureJobId(),
        message.getStatus());
    Job job = jobRepository.findBySecureJobId(message.getSecureJobId()).orElse(null);
    if (job == null) {
      log.warn("Received notification for unknown job: {}", message.getSecureJobId());
      return;
    }

    log.info("Processing notification alerts for job '{}' owned by '{}'", job.getName(), job.getUser().getEmail());
    List<NotificationChannel> channels = channelRepository.findByUser(job.getUser());
    log.info("Found {} notification channels registered for user", channels.size());

    for (NotificationChannel channel : channels) {
      for (AbstractNotificationHandler handler : handlers) {
        handler.processAndSend(message, job, channel);
      }
    }
  }
}
