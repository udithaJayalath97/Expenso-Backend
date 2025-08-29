package com.example.expenso.repository;

import com.example.expenso.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdAndReadStatus(Long userId, boolean readStatus);
    List<Notification> findByUserId(Long userId);
}
