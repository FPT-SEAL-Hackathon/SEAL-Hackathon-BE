package com.fpt.swp.sealhackathonbe.event.entity;

import com.fpt.swp.sealhackathonbe.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "Events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {
    @Id
    @Column(name = "EventID")
    private UUID eventId;

    @Column(name = "EventName", nullable = false)
    private String eventName;

    @Column(name = "Description")
    private String description;

    @Column(name = "Location", nullable = false)
    private String location;

    @Column(name = "BannerImageURL")
    private String bannerImageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EventStatusID", nullable = false)
    private EventStatus eventStatus;

    @Column(name = "RegistrationStart")
    private LocalDateTime registrationStart;

    @Column(name = "RegistrationEnd")
    private LocalDateTime registrationEnd;

    @Column(name = "EventStartDate")
    private LocalDate eventStartDate;

    @Column(name = "EventEndDate")
    private LocalDate eventEndDate;

    @Column(name = "MaxTeamSize", nullable = false)
    private Integer maxTeamSize;

    @Column(name = "MinTeamSize", nullable = false)
    private Integer minTeamSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CreatedByID", nullable = false)
    private User createdBy;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "IsDeleted", nullable = false)
    private Boolean isDeleted;

}
