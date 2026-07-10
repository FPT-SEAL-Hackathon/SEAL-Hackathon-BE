package com.fpt.swp.sealhackathonbe.settings.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Lưu trữ cài đặt hệ thống dạng key-value trong database.
 * Mỗi bản ghi là một cặp (settingKey, settingValue).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "SystemSettings")
public class SystemSetting {

    @Id
    @Column(name = "SettingKey", nullable = false, length = 100)
    private String settingKey;

    @Column(name = "SettingValue", nullable = false, length = 1000)
    private String settingValue;

    @Column(name = "SettingType", length = 20)
    private String settingType; // STRING, INTEGER, BOOLEAN

    @Column(name = "Description", length = 500)
    private String description;

    @UpdateTimestamp
    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;
}
