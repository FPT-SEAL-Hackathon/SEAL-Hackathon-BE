package com.fpt.swp.sealhackathonbe.team.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mốc tiến độ do mentor tạo và quản lý cho từng team.
 */
@Entity
@Table(name = "TeamMilestones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMilestone {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "MilestoneID", nullable = false, updatable = false)
    private UUID milestoneId;

    /** Team sở hữu milestone này. */
    @Column(name = "TeamID", nullable = false)
    private UUID teamId;

    /** Mentor đã tạo milestone. */
    @Column(name = "MentorUserID", nullable = false)
    private UUID mentorUserId;

    @Column(name = "Label", nullable = false, length = 255)
    private String label;

    /** true = mentor xác nhận đã hoàn thành. */
    @Column(name = "IsDone", nullable = false)
    @Builder.Default
    private Boolean isDone = false;

    /** Thứ tự sắp xếp trong danh sách. */
    @Column(name = "SortOrder", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @CreationTimestamp
    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt;
}
