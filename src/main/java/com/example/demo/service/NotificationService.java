/*
 * FILE: NotificationService.java
 * PURPOSE: Manages in-app notifications for users. Notifications are created
 *          by other services/controllers (e.g., after leave approval) and
 *          displayed per-user in the frontend notification bell.
 *
 * METHODS:
 *  - create(user, message, type, refId)
 *      Creates a new unread notification for the given user. `type` is a free-form
 *      string (e.g., "LEAVE", "ANNOUNCEMENT"). `refId` is an optional foreign key
 *      to the related entity. Called by various controllers after user-visible events.
 *
 *  - getAll(userId)
 *      Returns all notifications for a user ordered by creation date (newest first).
 *      Used to populate the full notification list dropdown.
 *
 *  - getUnread(userId)
 *      Returns only unread notifications for a user. Used to show the unread badge.
 *
 *  - countUnread(userId)
 *      Returns the count of unread notifications. Called frequently by the frontend
 *      polling endpoint to update the bell badge number.
 *
 *  - markRead(notificationId)
 *      Marks a single notification as read. Called when the user clicks a notification.
 *
 *  - markAllRead(userId)
 *      Marks all unread notifications for the user as read. Called from the
 *      "mark all as read" button in the notification panel.
 *
 * HOW TO MODIFY:
 *  - To add push/email notifications: inject a MailService or WebSocket handler
 *    and call it inside create() after persisting.
 *  - To add notification categories/icons: extend the `type` field usage in the
 *    frontend JS that renders notifications (app.js / employee.html).
 *  - To add deletion: add a delete(notificationId) method calling
 *    notificationRepository.deleteById(id).
 */
package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Notification;
import com.example.demo.entity.User;
import com.example.demo.repository.NotificationRepository;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public Notification create(User user, String message, String type, Long refId) {
        Notification n = new Notification();
        n.setUser(user);
        n.setMessage(message);
        n.setType(type);
        n.setRefId(refId);
        n.setIsRead(false);
        return notificationRepository.save(n);
    }

    public List<Notification> getAll(Long userId) {
        return notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getUnread(Long userId) {
        return notificationRepository.findByUser_IdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    public long countUnread(Long userId) {
        return notificationRepository.countByUser_IdAndIsReadFalse(userId);
    }

    public void markRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
    }

    public void markAllRead(Long userId) {
        List<Notification> unread = notificationRepository.findByUser_IdAndIsReadFalseOrderByCreatedAtDesc(userId);
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);
    }
}
