package com.shuu.berry.service;

import com.shuu.berry.dto.NotificationChannelRequestDTO;
import com.shuu.berry.dto.NotificationChannelResponseDTO;
import com.shuu.berry.entity.ChannelType;
import com.shuu.berry.entity.NotificationChannel;
import com.shuu.berry.entity.User;
import com.shuu.berry.repository.NotificationChannelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationChannelService {

  private static final String DISCORD_WEBHOOK_PREFIX = "https://discord.com/api/webhooks/";

  private final NotificationChannelRepository channelRepository;

  public List<NotificationChannelResponseDTO> getUserChannels(User user) {
    return channelRepository.findByUser(user).stream()
        .map(c -> new NotificationChannelResponseDTO(c.getId(), c.getChannelType(), c.getDestination()))
        .toList();
  }

  public NotificationChannelResponseDTO addChannel(User user, NotificationChannelRequestDTO req) {
    if (req.channelType() == ChannelType.DISCORD && !req.destination().startsWith(DISCORD_WEBHOOK_PREFIX)) {
      log.warn("Attempted to register invalid Discord webhook URL: {}", req.destination());
      throw new IllegalArgumentException("Invalid Discord webhook URL format");
    }

    NotificationChannel channel = NotificationChannel.builder()
        .user(user)
        .channelType(req.channelType())
        .destination(req.destination())
        .build();

    NotificationChannel saved = channelRepository.save(channel);
    log.info("Saved new notification channel (id={}) for user: {}", saved.getId(), user.getEmail());

    return new NotificationChannelResponseDTO(saved.getId(), saved.getChannelType(), saved.getDestination());
  }

  public void deleteChannel(User user, long id) {
    NotificationChannel channel = channelRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Notification channel not found"));

    if (channel.getUser().getId() != user.getId()) {
      log.warn("User {} attempted to delete channel {} owned by user {}", user.getId(), id, channel.getUser().getId());
      throw new SecurityException("You do not own this notification channel");
    }

    channelRepository.delete(channel);
    log.info("Deleted notification channel (id={}) for user: {}", id, user.getEmail());
  }
}
