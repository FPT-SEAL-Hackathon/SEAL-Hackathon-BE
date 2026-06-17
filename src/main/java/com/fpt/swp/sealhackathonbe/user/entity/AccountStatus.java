package com.fpt.swp.sealhackathonbe.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "accountStatus")
    private List<User> userAccountStatusList = new ArrayList<>();

    //tránh lỗi dữ liệu không đồng bộ (bị null)========
    public void addUser(User user){
        userAccountStatusList.add(user);
        user.setAccountStatus(this);
    }
    public void removeUser(User user){
        userAccountStatusList.remove(user);
        user.setAccountStatus(null);
    }
    //cách làm chuẩn====================================

    @Override
    public String toString() {
        return "AccountStatus{" +
                "statusId=" + statusId +
                ", statusName='" + statusName + '\'' +
                '}';
    }
}
