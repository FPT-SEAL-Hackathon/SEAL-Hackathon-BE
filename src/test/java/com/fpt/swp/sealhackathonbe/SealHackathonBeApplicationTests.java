package com.fpt.swp.sealhackathonbe;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Cung cấp URL ứng dụng bắt buộc để kiểm tra cấu hình khi chạy test.
@SpringBootTest(properties = {
        "app.frontend-url=https://frontend.example.test",
        "app.backend-url=https://backend.example.test"
})
class SealHackathonBeApplicationTests {

    @Test
    void contextLoads() {
    }

}
