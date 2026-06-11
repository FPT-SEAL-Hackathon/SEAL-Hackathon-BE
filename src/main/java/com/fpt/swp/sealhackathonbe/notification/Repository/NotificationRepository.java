package com.fpt.swp.sealhackathonbe.notification.Repository;

import com.fpt.swp.sealhackathonbe.notification.entity.Notification;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
