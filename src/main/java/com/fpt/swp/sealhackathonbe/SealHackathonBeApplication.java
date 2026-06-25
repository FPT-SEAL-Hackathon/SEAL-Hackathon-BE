package com.fpt.swp.sealhackathonbe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Điểm khởi động ứng dụng Spring Boot của SEAL Hackathon.
 */
@SpringBootApplication
// Refresh Token:
// Bật scheduler để job dọn dẹp token hết hạn có thể chạy nền.
@EnableScheduling
public class SealHackathonBeApplication {

    /**
     * Khởi tạo Spring context và embedded web server.
     */
    public static void main(String[] args) {
        SpringApplication.run(SealHackathonBeApplication.class, args);

    }

}
