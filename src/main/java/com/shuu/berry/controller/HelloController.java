package com.shuu.berry.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
  @GetMapping("/")
  public String helloWorld() {
    return "Hello, World! Berry is now correctly configured and running.";
  }

  @GetMapping("/2")
  public String Second() {
    return "Hello, World2! " + System.getProperty("testname") + "is now correctly configured and running.";

  }
}
