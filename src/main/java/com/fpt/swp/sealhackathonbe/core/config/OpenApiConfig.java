package com.fpt.swp.sealhackathonbe.core.config;


import org.springframework.context.annotation.Configuration;

@Configuration
<<<<<<< Updated upstream
=======
@OpenAPIDefinition(
        info = @Info(
                title = "SEAL Hackathon API",
                version = "1.0"
        ),
        security = {
                @SecurityRequirement(name = "bearerAuth")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Paste the accessToken only. Swagger UI adds the Bearer prefix automatically."
)
>>>>>>> Stashed changes
public class OpenApiConfig {
}
