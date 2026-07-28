package com.fpt.swp.sealhackathonbe.consultation.entity;

import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "TeamMentorNotes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"TeamID", "MentorID"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMentorNote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "NoteID")
    private UUID id;

    @Column(name = "TeamID", nullable = false)
    private UUID teamId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TeamID", nullable = false, insertable = false, updatable = false)
    private Teams team;

    @Column(name = "MentorID", nullable = false)
    private UUID mentorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MentorID", nullable = false, insertable = false, updatable = false)
    private User mentor;

    @Column(name = "Note", columnDefinition = "NVARCHAR(MAX)")
    private String note;

    @CreationTimestamp
    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;
}
