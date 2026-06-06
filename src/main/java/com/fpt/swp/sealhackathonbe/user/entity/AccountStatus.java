package com.fpt.swp.sealhackathonbe.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@Table(
        name = "AccountStatus",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "StatusName")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountStatus {

    @Id
    @Column(name = "StatusID", nullable = false, updatable = false)
    @GeneratedValue
    private UUID statusId;

    @Column(name = "StatusName", nullable = false, length = 50)
    private String statusName;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "accountStatus")
    private List<User> userAccountStatusList = new ArrayList<>();

    @Override
    public String toString() {
        return "AccountStatus{" +
                "statusId=" + statusId +
                ", statusName='" + statusName + '\'' +
                ", userAccountStatusList=" + userAccountStatusList +
                '}';
    }
}