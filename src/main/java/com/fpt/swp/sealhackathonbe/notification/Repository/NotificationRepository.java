package com.fpt.swp.sealhackathonbe.notification.Repository;

import com.fpt.swp.sealhackathonbe.notification.entity.Notification;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Page<Notification> findByRecipientUserIDOrderBySentAtDesc(User recipientUserID, Pageable pageable);

    Optional<Notification> findByIdAndRecipientUserID(UUID id, User recipientUserID);

    List<Notification> findByRecipientUserIDAndIsReadFalse(User recipientUserID);

    long countByRecipientUserIDAndIsReadFalse(User recipientUserID);

    // Hard delete user: xóa các notification user nhận.
    long deleteByRecipientUserID_UserId(UUID userId);

    // Hard delete user: notification user đã gửi cho NGƯỜI KHÁC được giữ lại
    // (SentByUserID NOT NULL) nên gán lại người gửi sang organizer thao tác.
    @Modifying
    @Query("UPDATE Notification n SET n.sentByUserID = :newSender WHERE n.sentByUserID.userId = :oldSenderId")
    int reassignSender(@Param("oldSenderId") UUID oldSenderId, @Param("newSender") User newSender);
}
