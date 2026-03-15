/*
 * FILE: Notification.java
 * PURPOSE: JPA entity for in-app notifications sent to a user. Maps to `notifications`.
 *
 * KEY FIELDS:
 *   - user    : The recipient User.
 *   - message : Human-readable notification text.
 *   - type    : String category tag (e.g., "BONUS", "LEAVE_APPROVED", "SUGGESTION",
 *               "ANNOUNCEMENT") — used for optional client-side filtering/icons.
 *   - refId   : Optional ID of the related entity (leave request ID, bonus ID, etc.).
 *   - isRead  : false until the user reads or marks all read.
 *
 * HOW TO MODIFY:
 *   - To create a notification, call NotificationService.create() from any controller or
 *     service — do not persist directly.
 *   - To add new notification types, just pass a new type string; no enum change needed.
 *   - Badge count on the frontend polls GET /api/shared/notifications/count which calls
 *     NotificationService.countUnread().
 */
package com.example.demo.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "notifications")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User user;

    @Column(columnDefinition = "TEXT")
    private String message;

    private String type; // BONUS, PENALTY, LEAVE_APPROVED, LEAVE_REJECTED, ANNOUNCEMENT, etc.
    private Long refId;
    private Boolean isRead = false;
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
