package com.fpt.swp.sealhackathonbe.event.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.swp.sealhackathonbe.core.exception.GlobalExceptionHandler;
import com.fpt.swp.sealhackathonbe.event.dto.response.EventResponse;
import com.fpt.swp.sealhackathonbe.event.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EventControllerCreateValidationTest {

    private MockMvc mockMvc;
    private EventService eventService;

    @BeforeEach
    void setUp() {
        eventService = mock(EventService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new EventController(eventService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createEventMissingLocationReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson().replace("\"location\":\"FPT University HCMC\",", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.location").value("Location is required"));
    }

    @Test
    void createEventMissingMinTeamSizeReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson().replace("\"minTeamSize\":2,", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.minTeamSize").value("Min team size is required"));
    }

    @Test
    void createEventMissingMaxTeamSizeReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson().replace(",\n  \"maxTeamSize\":5", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.maxTeamSize").value("Max team size is required"));
    }

    @Test
    void createEventDuplicateNameFromDatabaseReturns409() throws Exception {
        when(eventService.create(any())).thenThrow(new DataIntegrityViolationException(
                "Cannot insert duplicate key row in object 'dbo.Events' with unique index 'UQ_Events_EventName_Active'. EventName"
        ));

        mockMvc.perform(post("/api/v1/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("DUPLICATE_RESOURCE"))
                .andExpect(jsonPath("$.message").value("Event name already exists"));
    }

    @Test
    void createEventValidRequestReturns201() throws Exception {
        when(eventService.create(any())).thenReturn(EventResponse.builder()
                .eventId(UUID.randomUUID())
                .eventName("SEAL Hackathon 2030")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        mockMvc.perform(post("/api/v1/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventName", containsString("SEAL Hackathon")));
    }

    private String validJson() {
        return """
                {
                  "eventName":"SEAL Hackathon 2030",
                  "description":"Build software products",
                  "location":"FPT University HCMC",
                  "bannerImageUrl":"https://example.com/banner.png",
                  "eventStatusId":"30000000-0000-0000-0000-000000000001",
                  "registrationStart":"2030-06-01T08:00:00",
                  "registrationEnd":"2030-06-30T09:00:00",
                  "eventStartDate":"2030-06-30",
                  "eventEndDate":"2030-07-02",
                  "minTeamSize":2,
                  "maxTeamSize":5
                }
                """;
    }
}
