package com.shuu.berry.notification;

import com.shuu.berry.dto.NotificationMessage;
import com.shuu.berry.entity.ChannelType;
import com.shuu.berry.entity.Job;
import com.shuu.berry.entity.JobStatus;
import com.shuu.berry.entity.NotificationChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractNotificationHandler {

  public final void processAndSend(NotificationMessage message, Job job, NotificationChannel channel) {
    if (!shouldNotify(job, message.getStatus())) {
      log.info(
          "Notification skipped for job '{}' with status '{}' due to job settings (notifyOnFailure={}, notifyOnSuccess={})",
          job.getName(), message.getStatus(), job.isNotifyOnFailure(), job.isNotifyOnSuccess());
      return;
    }

    if (channel.getChannelType() != getSupportedChannelType()) {
      return;
    }

    log.info("Sending notification of type {} for job '{}' to {}", getSupportedChannelType(), job.getName(),
        channel.getDestination());
    String content = formatMessage(message, job);
    send(channel.getDestination(), content);
  }

  private boolean shouldNotify(Job job, JobStatus status) {
    return switch (status) {
      case FAILED -> job.isNotifyOnFailure();
      case SUCCEEDED -> job.isNotifyOnSuccess();
    };
  }

  protected abstract ChannelType getSupportedChannelType();

  protected abstract String formatMessage(NotificationMessage message, Job job);

  protected abstract void send(String destinationUrl, String content);
}
