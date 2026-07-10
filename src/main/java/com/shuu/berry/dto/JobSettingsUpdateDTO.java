package com.shuu.berry.dto;

public record JobSettingsUpdateDTO(
    boolean notifyOnFailure,
    boolean notifyOnSuccess) {
}
