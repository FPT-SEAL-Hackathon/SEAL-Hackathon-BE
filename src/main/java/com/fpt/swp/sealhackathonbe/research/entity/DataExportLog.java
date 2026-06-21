package com.fpt.swp.sealhackathonbe.research.entity;

import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "DataExportLog")
public class DataExportLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ExportID", nullable = false, updatable = false)
    private UUID exportId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "EventID", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ExportedByID", nullable = false)
    private User exportedBy;

    @CreationTimestamp
    @Column(name = "ExportedAt", nullable = false, updatable = false)
    private LocalDateTime exportedAt;

    @Nationalized
    @Column(name = "FileFormat", nullable = false, length = 10)
    private String fileFormat = "CSV";

    @Column(name = "\"RowCount\"")
    private Integer rowCount;

    @Nationalized
    @Column(name = "Notes", length = 500)
    private String notes;
}
