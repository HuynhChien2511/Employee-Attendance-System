/*
 * FILE: Task.java
 * PURPOSE: JPA entity representing a task that can be assigned to one or more employees.
 *          Maps to the `tasks` table in the database.
 *
 * FIELDS:
 *   - id            : Auto-generated primary key.
 *   - title         : Short name/title of the task (required).
 *   - description   : Full description or instructions for the task.
 *   - deadline      : Due date; if IN_PROCESS past this date, status auto-updates to MISSED_DEADLINE.
 *   - status        : Enum — IN_PROCESS | FINISHED | MISSED_DEADLINE. Defaults to IN_PROCESS.
 *   - createdBy     : The User (admin or manager) who created the task.
 *   - createdAt     : Timestamp set automatically on first persist.
 *   - attachedFileName / attachedFilePath : Optional file the creator attaches to the task.
 *
 * KEY METHOD:
 *   - prePersist()  : Runs before INSERT; sets createdAt and default status if they are null.
 *
 * HOW TO MODIFY:
 *   - Add a new field → add the field + getter/setter (or rely on Lombok @Data), then add a
 *     column in the relevant SQL script if not using ddl-auto=update.
 *   - Add a new status → extend the TaskStatus enum and update any switch/if logic in
 *     TaskApiController.updateMissedDeadlines() accordingly.
 *   - Rename the DB table → change @Table(name = "...") and update any native queries.
 */
package com.example.demo.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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
@Table(name = "tasks")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDate deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.IN_PROCESS;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "employee"})
    private User createdBy;

    private LocalDateTime createdAt;

    private String attachedFileName;

    private String attachedFilePath;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = TaskStatus.IN_PROCESS;
    }

    public enum TaskStatus {
        IN_PROCESS, FINISHED, MISSED_DEADLINE
    }
}
