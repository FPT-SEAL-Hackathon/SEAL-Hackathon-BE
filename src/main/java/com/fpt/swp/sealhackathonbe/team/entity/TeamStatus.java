package com.fpt.swp.sealhackathonbe.team.entity;

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
@Table(name = "TeamStatus")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeamStatus {

    // Bang danh muc trang thai cua team; Teams chi luu khoa ngoai StatusID.
    @Id
    @Column(name = "StatusID")
    private UUID statusId;

    @Column(name = "StatusName", nullable = false, length = 50)
    private String statusName;
}
