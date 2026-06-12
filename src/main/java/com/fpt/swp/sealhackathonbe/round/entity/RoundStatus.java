package com.fpt.swp.sealhackathonbe.round.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "RoundStatus")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoundStatus {
    @Id
    @Column(name = "StatusID")
    private UUID statusId;

    @Column(name = "StatusName")
    private String statusName;
}
