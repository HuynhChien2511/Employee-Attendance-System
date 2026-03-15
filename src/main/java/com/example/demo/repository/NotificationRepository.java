/*
 * FILE: NotificationRepository.java
 * PURPOSE: Spring Data JPA repository for Notification.
 *          Supports unread count (badge), listing all/unread notifications for a user.
 *
 * METHODS:
 *   - findByUser_IdOrderByCreatedAtDesc(userId)             : All notifications for a user, newest first.
 *   - findByUser_IdAndIsReadFalseOrderByCreatedAtDesc(userId): Only unread notifications.
 *                                                             Used for mark-all-read bulk load.
 *   - countByUser_IdAndIsReadFalse(userId)                  : Unread count used for the bell badge.
 *
 * HOW TO MODIFY:
 *   - To add notification expiry, add a `expiresAt` field to Notification and add a
 *     query here that filters out expired records.
 *   - The frontend polls /api/shared/notifications/count every 30 s; adjust polling
 *     frequency in employee.html if needed.
 */
package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUser_IdOrderByCreatedAtDesc(Long userId);
    List<Notification> findByUser_IdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
    long countByUser_IdAndIsReadFalse(Long userId);
}
