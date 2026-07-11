package com.shuu.berry.config;

import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

// NOTE: THIS is here to prevent any JSON bomb
@Configuration
public class JacksonSecurityConfig {

  @Bean
  @Primary
  public ObjectMapper objectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();
    objectMapper.getFactory().setStreamReadConstraints(
        StreamReadConstraints.builder()
            .maxStringLength(50000) // 50KB max string length
            .maxNestingDepth(20) // Prevent deep recursion
            .build());

    return objectMapper;
  }
}
