package com.shuu.berry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;

@SpringBootApplication
public class BerryApplication {

  public static void main(String[] args) {
    Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
    dotenv.entries().forEach((DotenvEntry entry) -> System.setProperty(
        entry.getKey(), entry.getValue()));
    SpringApplication.run(BerryApplication.class, args);
  }

}
