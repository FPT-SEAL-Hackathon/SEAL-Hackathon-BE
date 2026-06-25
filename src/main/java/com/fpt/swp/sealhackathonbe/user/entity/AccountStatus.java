package com.fpt.swp.sealhackathonbe.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Biểu diễn trạng thái vòng đời tài khoản như Unverified hoặc Active.
 */
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

    /**
     * Đồng bộ quan hệ hai chiều khi gán trạng thái cho user.
     */
    public void addUser(User user) {
        userAccountStatusList.add(user);
        user.setAccountStatus(this);
    }

    /**
     * Gỡ trạng thái khỏi user và giữ quan hệ hai chiều nhất quán.
     */
    public void removeUser(User user) {
        userAccountStatusList.remove(user);
        user.setAccountStatus(null);
    }

    /**
     * Tránh load danh sách user khi log/debug trạng thái.
     */
    @Override
    public String toString() {
        return "AccountStatus{" +
                "statusId=" + statusId +
                ", statusName='" + statusName + '\'' +
                '}';
    }
}
