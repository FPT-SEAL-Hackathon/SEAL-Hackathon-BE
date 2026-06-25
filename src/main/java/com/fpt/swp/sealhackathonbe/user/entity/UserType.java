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
 * Biểu diễn nhóm người dùng dùng làm nguồn role trong hệ thống.
 */
@Entity
@Table(
        name = "UserType",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "TypeName")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserType {

    @Id
    @Column(name = "UserTypeID", nullable = false, updatable = false)
    @GeneratedValue
    private UUID userTypeId;

    @Column(name = "TypeName", nullable = false, length = 50)
    private String typeName;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "userType")
    private List<User> userTypeList = new ArrayList<>();

    /**
     * Đồng bộ quan hệ hai chiều khi gán user vào type.
     */
    public void addUser(User user) {
        userTypeList.add(user);
        user.setUserType(this);
    }

    /**
     * Gỡ user khỏi type và giữ quan hệ hai chiều nhất quán.
     */
    public void removeUser(User user) {
        userTypeList.remove(user);
        user.setUserType(null);
    }

    /**
     * Tránh load danh sách user khi log/debug loại người dùng.
     */
    @Override
    public String toString() {
        return "UserType{" +
                "userTypeId=" + userTypeId +
                ", typeName='" + typeName + '\'' +
                '}';
    }
}
