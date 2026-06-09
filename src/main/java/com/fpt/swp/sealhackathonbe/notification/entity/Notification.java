//package com.fpt.swp.sealhackathonbe.notification.entity;
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
//@Table(name = "Notifications")
//public class Notification {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "NotificationID", nullable = false)
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "EventID")
//    private Event eventID;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "RecipientUserID")
//    private User recipientUserID;
//
//    @Size(max = 300)
//    @NotNull
//    @Nationalized
//    @Column(name = "Title", nullable = false, length = 300)
//    private String title;
//
//    @NotNull
//    @Nationalized
//    @Lob
//    @Column(name = "Body", nullable = false)
//    private String body;
//
//    @NotNull
//    @ColumnDefault("getutcdate()")
//    @Column(name = "SentAt", nullable = false)
//    private Instant sentAt;
//
//    @NotNull
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "SentByUserID", nullable = false)
//    private User sentByUserID;
//
//    @NotNull
//    @ColumnDefault("0")
//    @Column(name = "IsRead", nullable = false)
//    private Boolean isRead;
//
//
//}