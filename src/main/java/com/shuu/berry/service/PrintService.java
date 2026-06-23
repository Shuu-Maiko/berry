package com.shuu.berry.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.jobrunr.jobs.annotations.Job;
import org.springframework.stereotype.Service;

@Service
public class PrintService {

  // @Recurring(id = "print", interval = "PT16S")
  // @Job(name = "Second Counter")
  // public synchronized void printEverySecond() {
  // LocalDateTime currentDateTime = LocalDateTime.now();
  // DateTimeFormatter customFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd
  // HH:mm:ss");
  //
  // System.out.println("Printed at : " + currentDateTime.format(customFormat));
  // }

  @Job(name = "Dynamic Print Job")
  public void printMessage(String jobId, String message) {
    LocalDateTime currentDateTime = LocalDateTime.now();
    DateTimeFormatter customFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    System.out.println("[" + currentDateTime.format(customFormat) + "] Job " + jobId + " says: " + message);
  }
}
