package com.amisimecompila.speisandbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class SpeisandboxApplication {

    public static void main(String[] args) {

        SpringApplication.run(SpeisandboxApplication.class, args);

    }
}
