package com.shuu.berry.entity;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum JobStatus {
  FAILED,
  SUCCEEDED;

  private static final Map<String, JobStatus> LOOKUP = Stream.of(values())
      .collect(Collectors.toMap(Enum::name, s -> s));

  public static JobStatus fromStateName(String name) {
    return LOOKUP.get(name);
  }
}
