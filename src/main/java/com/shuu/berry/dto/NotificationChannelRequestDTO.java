package com.shuu.berry.dto;

import com.shuu.berry.entity.ChannelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificationChannelRequestDTO(
    @NotNull(message = "Channel type is required") ChannelType channelType,

    @NotBlank(message = "Destination is required") String destination) {
}
