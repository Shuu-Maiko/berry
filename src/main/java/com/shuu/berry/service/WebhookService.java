package com.shuu.berry.service;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import org.jobrunr.jobs.annotations.Job;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import com.shuu.berry.entity.JobResponseLog;
import com.shuu.berry.entity.WebhookHttpMethod;
import com.shuu.berry.repository.JobResponseLogRepository;
import com.shuu.berry.security.SsrfValidator;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class WebhookService {

  private final JobResponseLogRepository jobResponseLogRepository;
  private final SsrfValidator ssrfValidator;
  private final RestClient restClient;

  public WebhookService(JobResponseLogRepository jobResponseLogRepository, SsrfValidator ssrfValidator) {
    this.jobResponseLogRepository = jobResponseLogRepository;
    this.ssrfValidator = ssrfValidator;

    HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();

    JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
    factory.setReadTimeout(Duration.ofSeconds(5));

    this.restClient = RestClient.builder().requestFactory(factory).build();
  }

  @Job(name = "Webhook Service", retries = 5)
  public void sendRequest(String jobId, String url, WebhookHttpMethod customHttpMethod, Map<String, String> httpHeaders,
      String payload) {

    try {
      ssrfValidator.validateUrl(url);
      HttpMethod httpMethod = HttpMethod.valueOf(customHttpMethod.name());
      RestClient.RequestBodySpec request = restClient.method(httpMethod).uri(url);
      if (httpHeaders != null && !httpHeaders.isEmpty()) {
        request.headers(headers -> headers.setAll(httpHeaders));
      }

      if (httpMethod.name().matches("POST|PUT|PATCH") && payload != null && !payload.isBlank()) {
        request.contentType(org.springframework.http.MediaType.APPLICATION_JSON);
        request.body(payload);
      }

      ResponseEntity<String> response = request.retrieve().toEntity(String.class);
      int statusCode = response.getStatusCode().value();
      String body = response.getBody();
      saveResponseLog(jobId, statusCode, body);
      log.info("Job {} successfully pinged {}", jobId, url);
    } catch (org.springframework.web.client.RestClientResponseException e) {
      int statusCode = e.getStatusCode().value();
      String errorBody = e.getResponseBodyAsString();
      saveResponseLog(jobId, statusCode, errorBody);
      log.warn("Job {} failed ping to {} with status {}", jobId, url, statusCode);
      throw e;
    } catch (SecurityException e) {
      saveResponseLog(jobId, 0, e.getMessage());
      log.error("Job {} SSRF Blocked: {}", jobId, e.getMessage());
      throw e;
    } catch (Exception e) {
      saveResponseLog(jobId, 0, e.getMessage());
      log.error("Job {} failed network connection to {}: {}", jobId, url, e.getMessage());
      throw e;
    }
  }

  private void saveResponseLog(String secureJobId, int statusCode, String body) {
    String truncatedBody = null;
    if (body != null) {
      truncatedBody = body.length() > 2000 ? body.substring(0, 2000) : body;
    }

    JobResponseLog responseLog = JobResponseLog.builder()
        .secureJobId(secureJobId)
        .httpStatus(statusCode)
        .responseBody(truncatedBody)
        .createdAt(LocalDateTime.now())
        .build();

    jobResponseLogRepository.save(responseLog);
  }
}
