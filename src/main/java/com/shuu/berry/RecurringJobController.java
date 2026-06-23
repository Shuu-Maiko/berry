package com.shuu.berry;

import org.jobrunr.scheduling.JobScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.shuu.berry.service.PrintService;
import com.shuu.berry.utils.UuidUtil; // Import our professional utility class!

import lombok.Data;

@Data
class JobRequestDTO {
  private String name;
  private String cronString;
  private String message;
}

@RestController
@RequestMapping("/api/jobs")
public class RecurringJobController {
  @Autowired
  private JobScheduler jobScheduler;
  @Autowired
  private PrintService printService;

  @PostMapping("/create")
  public String addJob(@RequestBody JobRequestDTO req) {

    String secureJobId = UuidUtil.generateUuidV7().toString();
    jobScheduler.scheduleRecurrently(
        secureJobId,
        req.getCronString(),
        () -> printService.printMessage(secureJobId, req.getMessage()));

    return "Successfully scheduled Job [" + req.getName() + "] with secure ID: " + secureJobId
        + " to run on schedule: " + req.getCronString();
  }
}
