package com.dev.notification.service;

import com.dev.notification.dto.NotificationResponse;
import com.dev.notification.model.NotificationType;
import com.dev.user.model.User;

import java.util.List;

public interface NotificationService {
    void createNotification(User user, String title, String message, NotificationType type);
    
    List<NotificationResponse> getMyNotifications(Long userId);
    
    void markAsRead(Long notificationId, Long userId);
    
    long countUnreadNotifications(Long userId);
}
