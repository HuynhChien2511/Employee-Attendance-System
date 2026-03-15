/*
 * FILE: Announcement.java
 * PURPOSE: JPA entity representing a broadcast announcement. Maps to `announcements`.
 *
 * KEY FIELDS:
 *   - sender      : The User (admin or manager) who created the announcement.
 *   - targetType  : ALL | DEPARTMENT | EMPLOYEE — controls who can see this announcement.
 *   - targetValue : Meaning depends on targetType:
 *                     ALL        → null (visible to everyone).
 *                     DEPARTMENT → department name string.
 *                     EMPLOYEE   → employee.id as a string.
 *   - title / content : The announcement text.
 *
 * HOW TO MODIFY:
 *   - To add a new target scope (e.g., ROLE), extend the TargetType enum and add the
 *     corresponding filter branch in AnnouncementRepository's @Query methods and in
 *     AnnouncementService.getVisibleToEmployee() / getVisibleToManager().
 *   - Visibility filtering is performed in AnnouncementRepository with JPQL; update
 *     those queries whenever targetType semantics change.
 */
package com.example.demo.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "announcements")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "employee"})
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TargetType targetType;

    private String targetValue; // null=ALL, dept name for DEPARTMENT, employee.id string for EMPLOYEE

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public enum TargetType {
        ALL, DEPARTMENT, EMPLOYEE
    }
}
