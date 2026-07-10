package com.shuu.berry.config;

import org.jobrunr.jobs.filters.JobFilter;
import org.jobrunr.server.BackgroundJobServer;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class JobRunrFilterConfig {

  public JobRunrFilterConfig(BackgroundJobServer backgroundJobServer, List<? extends JobFilter> jobFilters) {
    backgroundJobServer.getJobFilters().addAll(jobFilters);
  }
}
