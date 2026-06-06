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

    @Override
    public String toString() {
        return "UserType{" +
                "userTypeId=" + userTypeId +
                ", typeName='" + typeName + '\'' +
                ", userTypeList=" + userTypeList +
                '}';
    }
}