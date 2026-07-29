package com.fpt.swp.sealhackathonbe.settings.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "FptStudentCodePrefixes")
public class FptStudentCodePrefix {

    @Id
    @Column(name = "Prefix", nullable = false, length = 2)
    private String prefix;

    @Column(name = "EnglishName", nullable = false, length = 100)
    private String englishName;

    @Column(name = "VietnameseName", nullable = false, length = 200)
    private String vietnameseName;

    @Column(name = "MajorGroup", nullable = false, length = 100)
    private String majorGroup;

    @Column(name = "MajorCode", length = 20)
    private String majorCode;

    @Column(name = "Note", length = 500)
    private String note;

    @Column(name = "IsActive", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;
}
