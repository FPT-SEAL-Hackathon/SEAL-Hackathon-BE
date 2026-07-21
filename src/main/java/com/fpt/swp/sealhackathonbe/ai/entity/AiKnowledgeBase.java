package com.fpt.swp.sealhackathonbe.ai.entity;

import com.fpt.swp.sealhackathonbe.category.entity.Category;
import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "AI_Knowledge_Base")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiKnowledgeBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EventID", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CategoryID")
    private Category category;

    @Column(name = "QuestionPattern", nullable = false, columnDefinition = "TEXT")
    private String questionPattern;

    @Column(name = "StandardAnswer", nullable = false, columnDefinition = "TEXT")
    private String standardAnswer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MentorID", nullable = false)
    private User mentor;

    @CreationTimestamp
    @Column(name = "CreatedAt", updatable = false)
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt")
    private Date updatedAt;
}
