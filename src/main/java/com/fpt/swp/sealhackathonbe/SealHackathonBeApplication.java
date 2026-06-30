package com.fpt.swp.sealhackathonbe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Điểm khởi động ứng dụng Spring Boot của SEAL Hackathon.
 */
@SpringBootApplication
// Cấu hình URL ứng dụng:
// Quét cấu hình typed dùng cho URL frontend/backend theo từng môi trường.
@ConfigurationPropertiesScan
// Token làm mới:
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
