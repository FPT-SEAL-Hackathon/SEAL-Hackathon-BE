package com.fpt.swp.sealhackathonbe.event.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "EventStatus")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EventStatus {
    @Id
    @Column(name = "StatusID")
    private UUID eventStatusId;

    @Column(name = "StatusName")
    private String eventStatusName;
}
