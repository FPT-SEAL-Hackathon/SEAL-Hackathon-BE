package com.fpt.swp.sealhackathonbe;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Cung cấp URL ứng dụng bắt buộc để kiểm tra cấu hình khi chạy test.
@SpringBootTest(properties = {
        "app.frontend-url=https://frontend.example.test",
        "app.backend-url=https://backend.example.test",
        "spring.datasource.url=jdbc:h2:mem:seal_hackathon_test;DB_CLOSE_DELAY=-1;MODE=MSSQLServer;DATABASE_TO_UPPER=false",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.show-sql=false",
        "spring.security.oauth2.client.registration.google.client-id=test-google-client-id",
        "spring.security.oauth2.client.registration.google.client-secret=test-google-client-secret",
        "spring.security.oauth2.client.registration.github.client-id=test-github-client-id",
        "spring.security.oauth2.client.registration.github.client-secret=test-github-client-secret",
        "jwt.secret=test-jwt-secret-key-for-context-loads"
})
class SealHackathonBeApplicationTests {

    @Test
    void contextLoads() {
    }

}
