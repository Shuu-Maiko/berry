package com.shuu.berry.utils;

import java.util.UUID;
import com.github.f4b6a3.uuid.UuidCreator;

public final class UuidUtil {
  private UuidUtil() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  public static UUID generateUuidV7() {
    return UuidCreator.getTimeOrderedEpoch();
  }
}
