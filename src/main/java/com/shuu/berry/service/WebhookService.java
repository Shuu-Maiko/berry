package com.shuu.berry.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.jobrunr.jobs.annotations.Job;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import com.shuu.berry.entity.WebhookHttpMethod;

@Service
public class WebhookService {

  private final RestClient restClient;

  public WebhookService() {
    this.restClient = RestClient.create();
  }

  @Job(name = "Webhook Service", retries = 5)
  public void sendRequest(String jobId, String url, WebhookHttpMethod customHttpMethod, Map<String, String> httpHeaders,
      String payload) {

    HttpMethod httpMethod = HttpMethod.valueOf(customHttpMethod.name());
    RestClient.RequestBodySpec request = restClient.method(httpMethod).uri(url);

    if (httpHeaders != null && !httpHeaders.isEmpty()) {
      request.headers(headers -> headers.setAll(httpHeaders));
    }

    if (httpMethod.name().matches("POST|PUT|PATCH") && payload != null && !payload.isBlank()) {
      request.contentType(org.springframework.http.MediaType.APPLICATION_JSON);
      request.body(payload);
    }

    String response = request.retrieve().body(String.class);

    LocalDateTime currentDateTime = LocalDateTime.now();
    DateTimeFormatter customFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    System.out.println(
        "[" + currentDateTime.format(customFormat) + "] Job " + jobId + " pinged: " + url + " and received: "
            + response);
  }

}
