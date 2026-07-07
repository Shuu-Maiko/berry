package com.shuu.berry.notification;

import com.shuu.berry.dto.NotificationMessage;
import com.shuu.berry.entity.ChannelType;
import com.shuu.berry.entity.Job;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Component
public class DiscordNotificationHandler extends AbstractNotificationHandler {

  private static final String DISCORD_WEBHOOK_PREFIX = "https://discord.com/api/webhooks/";

  private final RestClient restClient = RestClient.create();

  @Override
  protected ChannelType getSupportedChannelType() {
    return ChannelType.DISCORD;
  }

  @Override
  protected String formatMessage(NotificationMessage message, Job job) {
    return "```\n" +
        "Berry Job Alert\n" +
        "Job:     " + job.getName() + "\n" +
        "Status:  " + message.getStatus() + "\n" +
        "Details: " + message.getMessage() + "\n" +
        "```";
  }

  @Override
  protected void send(String destinationUrl, String content) {

    if (!destinationUrl.startsWith(DISCORD_WEBHOOK_PREFIX)) {
      log.warn("Blocked non-Discord webhook URL: {}", destinationUrl);
      return;
    }

    try {
      restClient.post()
          .uri(destinationUrl)
          .body(Map.of("content", content))
          .retrieve()
          .toBodilessEntity();
    } catch (HttpClientErrorException e) {

      log.warn("Discord rejected notification ({}): {}", e.getStatusCode(), e.getMessage());
    }

  }
}
