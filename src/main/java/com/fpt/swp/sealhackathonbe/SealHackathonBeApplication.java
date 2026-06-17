package com.fpt.swp.sealhackathonbe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class SealHackathonBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SealHackathonBeApplication.class, args);

    }

}
