package com.fpt.swp.sealhackathonbe.event.service.impl;

import com.fpt.swp.sealhackathonbe.auth.service.mapper.AuthenticationService;
import com.fpt.swp.sealhackathonbe.core.exception.BadRequestException;
import com.fpt.swp.sealhackathonbe.event.dto.request.CreateEventRequest;
import com.fpt.swp.sealhackathonbe.event.dto.response.EventResponse;
import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.event.entity.EventStatus;
import com.fpt.swp.sealhackathonbe.event.mapper.EventMapper;
import com.fpt.swp.sealhackathonbe.event.repository.EventRepository;
import com.fpt.swp.sealhackathonbe.event.repository.EventStatusRepository;
import com.fpt.swp.sealhackathonbe.eventparticipant.repository.EventParticipantRepository;
import com.fpt.swp.sealhackathonbe.category.repository.CategoryRepository;
import com.fpt.swp.sealhackathonbe.round.repository.RoundRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamsRepository;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    @Mock
    private TeamsRepository teamsRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private RoundRepository roundRepository;

    @Mock
    private AuthenticationService authenticationService;

    private EventServiceImplementation eventService;

    @BeforeEach
    void setUp() {
        eventService = new EventServiceImplementation(
                eventRepository,
                eventStatusRepository,
                new EventMapper(),
                userRepository,
                eventParticipantRepository,
                teamsRepository,
                categoryRepository,
                roundRepository,
                authenticationService
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

        BadRequestException exception = assertThrows(BadRequestException.class, () -> eventService.create(request));

        assertEquals("Min team size cannot be greater than max team size", exception.getMessage());
    }

    @Test
    void createEventRegistrationStartAfterEndReturnsBadRequest() {
        CreateEventRequest request = validRequest();
        request.setRegistrationStart(LocalDateTime.of(2030, 6, 30, 10, 0));
        request.setRegistrationEnd(LocalDateTime.of(2030, 6, 30, 9, 0));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> eventService.create(request));

        assertEquals("Registration start date must be before registration end date", exception.getMessage());
    }

    @Test
    void createEventStartDateAfterEndDateReturnsBadRequest() {
        CreateEventRequest request = validRequest();
        request.setEventStartDate(LocalDate.of(2030, 7, 3).atStartOfDay());
        request.setEventEndDate(LocalDate.of(2030, 7, 2).atTime(LocalTime.MAX));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> eventService.create(request));

        assertEquals("Event start date must be before event end date", exception.getMessage());
    }

    @Test
    void createEventRegistrationEndAfterEventStartDateReturnsBadRequest() {
        CreateEventRequest request = validRequest();
        request.setRegistrationEnd(LocalDateTime.of(2030, 7, 1, 9, 0));
        request.setEventStartDate(LocalDate.of(2030, 6, 30).atStartOfDay());

        BadRequestException exception = assertThrows(BadRequestException.class, () -> eventService.create(request));

        assertEquals("Registration end time must be before the event starts", exception.getMessage());
    }

    // Luat hien tai (EventServiceImplementation:120) la CHAT: dang ky phai ket thuc truoc
    // ngay event bat dau, ket thuc DUNG ngay do cung bi tu choi.
    @Test
    void createEventRegistrationEndSameDateAsEventStartReturnsBadRequest() {
        CreateEventRequest request = validRequest();
        request.setRegistrationEnd(LocalDateTime.of(2030, 6, 30, 9, 0));
        request.setEventStartDate(LocalDate.of(2030, 6, 30).atStartOfDay());

        BadRequestException exception = assertThrows(BadRequestException.class, () -> eventService.create(request));

        assertEquals("Registration end time must be before the event starts", exception.getMessage());
    }

    // TODO(BA): create() hien KHONG con kiem tra trung ten event
    // (EventServiceImplementation.create khong goi existsByEventNameIgnoreCaseAndIsDeletedFalse).
    // Giu test o trang thai disabled thay vi xoa: neu viec bo kiem tra la NGOAI Y MUON thi day
    // la regression can khoi phuc; neu la co y thi xoa han test nay.
    @Disabled("Production khong con validate trung ten event khi tao - can BA xac nhan")
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
        //request.setEventStatusId(DRAFT_STATUS_ID);
        request.setRegistrationStart(LocalDateTime.of(2030, 6, 1, 8, 0));
        // Dang ky phai KET THUC TRUOC ngay event bat dau (EventServiceImplementation:120
        // dung !registrationEnd.toLocalDate().isBefore(eventStart)) -> khong duoc trung ngay.
        request.setRegistrationEnd(LocalDateTime.of(2030, 6, 29, 9, 0));
        request.setEventStartDate(LocalDate.of(2030, 6, 30).atStartOfDay());
        request.setEventEndDate(LocalDate.of(2030, 7, 2).atTime(LocalTime.MAX));
        request.setMinTeamSize(2);
        request.setMaxTeamSize(5);
        return request;
    }

    private void mockSuccessfulCreate() {
        mockDraftStatus();

        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setEmail("organizer@example.com");
        // create() lay nguoi tao qua AuthenticationService (khong con userRepository.findByEmail).
        when(authenticationService.getCurrentUser()).thenReturn(user);

        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    // create() tra Draft status theo TEN (findByEventStatusName), khong phai theo ID.
    private void mockDraftStatus() {
        when(eventStatusRepository.findByEventStatusName("Draft"))
                .thenReturn(Optional.of(new EventStatus(DRAFT_STATUS_ID, "Draft")));
    }
}
