package com.fpt.swp.sealhackathonbe.event.service.impl;

import com.fpt.swp.sealhackathonbe.core.exception.BadRequestException;
import com.fpt.swp.sealhackathonbe.event.dto.request.CreateEventRequest;
import com.fpt.swp.sealhackathonbe.event.dto.response.EventResponse;
import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.event.entity.EventStatus;
import com.fpt.swp.sealhackathonbe.event.mapper.EventMapper;
import com.fpt.swp.sealhackathonbe.event.repository.EventRepository;
import com.fpt.swp.sealhackathonbe.event.repository.EventStatusRepository;
import com.fpt.swp.sealhackathonbe.eventparticipant.repository.EventParticipantRepository;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceImplementationCreateTest {

    private static final UUID DRAFT_STATUS_ID = UUID.fromString("30000000-0000-0000-0000-000000000001");

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventStatusRepository eventStatusRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventParticipantRepository eventParticipantRepository;

    private EventServiceImplementation eventService;

    @BeforeEach
    void setUp() {
        eventService = new EventServiceImplementation(
                eventRepository,
                eventStatusRepository,
                new EventMapper(),
                userRepository,
                eventParticipantRepository
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("organizer@example.com", "password")
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createEventMinTeamSizeGreaterThanMaxTeamSizeReturnsBadRequest() {
        CreateEventRequest request = validRequest();
        request.setMinTeamSize(6);
        request.setMaxTeamSize(5);
        mockDraftStatus();

        BadRequestException exception = assertThrows(BadRequestException.class, () -> eventService.create(request));

        assertEquals("Min team size cannot be greater than max team size", exception.getMessage());
    }

    @Test
    void createEventRegistrationStartAfterEndReturnsBadRequest() {
        CreateEventRequest request = validRequest();
        request.setRegistrationStart(LocalDateTime.of(2030, 6, 30, 10, 0));
        request.setRegistrationEnd(LocalDateTime.of(2030, 6, 30, 9, 0));
        mockDraftStatus();

        BadRequestException exception = assertThrows(BadRequestException.class, () -> eventService.create(request));

        assertEquals("Registration start time must be before or equal to registration end time", exception.getMessage());
    }

    @Test
    void createEventStartDateAfterEndDateReturnsBadRequest() {
        CreateEventRequest request = validRequest();
        request.setEventStartDate(LocalDate.of(2030, 7, 3));
        request.setEventEndDate(LocalDate.of(2030, 7, 2));
        mockDraftStatus();

        BadRequestException exception = assertThrows(BadRequestException.class, () -> eventService.create(request));

        assertEquals("Event start date must be before or equal to event end date", exception.getMessage());
    }

    @Test
    void createEventRegistrationEndSameDateAsEventStartWithTimeIsValid() {
        CreateEventRequest request = validRequest();
        request.setRegistrationEnd(LocalDateTime.of(2030, 6, 30, 9, 0));
        request.setEventStartDate(LocalDate.of(2030, 6, 30));
        mockSuccessfulCreate();

        EventResponse response = eventService.create(request);

        assertEquals("SEAL Hackathon 2030", response.getEventName());
    }

    @Test
    void createEventDuplicateNameReturnsBadRequest() {
        CreateEventRequest request = validRequest();
        mockDraftStatus();
        when(eventRepository.existsByEventNameIgnoreCaseAndIsDeletedFalse("SEAL Hackathon 2030")).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> eventService.create(request));

        assertEquals("Event name already exists", exception.getMessage());
    }

    @Test
    void createEventValidRequestSavesEvent() {
        CreateEventRequest request = validRequest();
        mockSuccessfulCreate();

        EventResponse response = eventService.create(request);

        assertEquals("SEAL Hackathon 2030", response.getEventName());
        assertEquals("FPT University HCMC", response.getLocation());
    }

    private CreateEventRequest validRequest() {
        CreateEventRequest request = new CreateEventRequest();
        request.setEventName("SEAL Hackathon 2030");
        request.setDescription("Build software products");
        request.setLocation("FPT University HCMC");
        request.setBannerImageUrl("https://example.com/banner.png");
        request.setEventStatusId(DRAFT_STATUS_ID);
        request.setRegistrationStart(LocalDateTime.of(2030, 6, 1, 8, 0));
        request.setRegistrationEnd(LocalDateTime.of(2030, 6, 30, 9, 0));
        request.setEventStartDate(LocalDate.of(2030, 6, 30));
        request.setEventEndDate(LocalDate.of(2030, 7, 2));
        request.setMinTeamSize(2);
        request.setMaxTeamSize(5);
        return request;
    }

    private void mockSuccessfulCreate() {
        mockDraftStatus();
        when(eventRepository.existsByEventNameIgnoreCaseAndIsDeletedFalse("SEAL Hackathon 2030")).thenReturn(false);

        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setEmail("organizer@example.com");
        when(userRepository.findByEmail("organizer@example.com")).thenReturn(user);

        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void mockDraftStatus() {
        when(eventStatusRepository.findById(DRAFT_STATUS_ID))
                .thenReturn(Optional.of(new EventStatus(DRAFT_STATUS_ID, "Draft")));
    }
}
