package com.shuu.berry.dto;

import com.shuu.berry.entity.ChannelType;

public record NotificationChannelResponseDTO(
    long id,
    ChannelType channelType,
    String destination) {
}
