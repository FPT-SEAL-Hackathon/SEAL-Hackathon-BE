package com.fpt.swp.sealhackathonbe.category.entity;

import com.fpt.swp.sealhackathonbe.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "CategoryMentors")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CategoryMentor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "CategoryMentorID")
    private UUID categoryMentorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CategoryID", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MentorUserID", nullable = false)
    private User mentor;

    @Column(name = "AssignedAt")
    private LocalDateTime assignedAt;
}
