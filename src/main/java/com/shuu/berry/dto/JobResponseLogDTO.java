package com.shuu.berry.dto;

import java.time.LocalDateTime;

public record JobResponseLogDTO(
    String secureJobId,
    Integer httpStatus,
    String responseBody,
    LocalDateTime createdAt
) {}
