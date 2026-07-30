package com.fpt.swp.sealhackathonbe.core.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.IOException;
import java.util.UUID;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(uppercaseUuidModule());
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.findAndRegisterModules();
        return objectMapper;
    }

    @Bean
    public SimpleModule uppercaseUuidModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(UUID.class, new JsonSerializer<UUID>() {
            @Override
            public void serialize(UUID value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                if (value != null) {
                    gen.writeString(value.toString().toUpperCase());
                } else {
                    gen.writeNull();
                }
            }
        });
        return module;
    }
}
