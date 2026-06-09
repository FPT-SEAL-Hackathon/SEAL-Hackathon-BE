//package com.fpt.swp.sealhackathonbe.research.entity;
//
//import com.fpt.swp.sealhackathonbe.user.entity.User;
//import jakarta.persistence.*;
//import jakarta.validation.constraints.NotNull;
//import jakarta.validation.constraints.Size;
//import lombok.Getter;
//import lombok.Setter;
//import org.hibernate.annotations.ColumnDefault;
//import org.hibernate.annotations.Nationalized;
//
//import java.time.Instant;
//
//@Getter
//@Setter
//@Entity
//@Table(name = "DataExportLog")
//public class DataExportLog {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "ExportID", nullable = false)
//    private Integer id;
//
//    @NotNull
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "EventID", nullable = false)
//    private Event eventID;
//
//    @NotNull
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "ExportedByID", nullable = false)
//    private User exportedByID;
//
//    @NotNull
//    @ColumnDefault("getutcdate()")
//    @Column(name = "ExportedAt", nullable = false)
//    private Instant exportedAt;
//
//    @Size(max = 10)
//    @NotNull
//    @Nationalized
//    @ColumnDefault("'CSV'")
//    @Column(name = "FileFormat", nullable = false, length = 10)
//    private String fileFormat;
//
//    @Column(name = "\"RowCount\"")
//    private Integer rowCount;
//
//    @Size(max = 500)
//    @Nationalized
//    @Column(name = "Notes", length = 500)
//    private String notes;
//
//
//}