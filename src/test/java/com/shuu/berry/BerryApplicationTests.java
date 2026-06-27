package com.shuu.berry;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "jobrunr.dashboard.enabled=false")
class BerryApplicationTests {

  @Test
  void contextLoads() {
  }

}
