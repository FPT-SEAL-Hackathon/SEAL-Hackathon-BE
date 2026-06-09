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

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "userType")
    private List<User> userTypeList = new ArrayList<>();

    public void addUser(User user) {
        userTypeList.add(user);
        user.setUserType(this);
    }
    public void removeUser(User user){
        userTypeList.remove(user);
        user.setUserType(null);
    }

    @Override
    public String toString() {
        return "UserType{" +
                "userTypeId=" + userTypeId +
                ", typeName='" + typeName + '\'' +
                ", userTypeList=" + userTypeList +
                '}';
    }
}