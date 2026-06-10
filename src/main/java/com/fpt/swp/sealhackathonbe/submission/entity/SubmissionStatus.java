package com.fpt.swp.sealhackathonbe.submission.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "SubmissionStatus")
public class SubmissionStatus {
    @Id
    @Column(name = "StatusID")
    private UUID statusId;

    @Column(name = "StatusName", nullable = false, length = 50)
    private String statusName;
}
