/*
 * FILE: TaskSubmission.java
 * PURPOSE: JPA entity representing a submission made by an employee for a specific task.
 *          Maps to the `task_submissions` table.
 *
 * FIELDS:
 *   - id          : Auto-generated primary key.
 *   - task        : The task this submission belongs to (lazy-loaded).
 *   - employee    : The employee who submitted (lazy-loaded).
 *   - textContent : Written submission text (may be null if employee only attaches a file).
 *   - fileName    : Original filename of the uploaded file (display name).
 *   - filePath    : Relative path under the upload directory where the file is stored.
 *   - submittedAt : Timestamp set automatically on first persist.
 *
 * KEY METHOD:
 *   - prePersist() : Sets submittedAt to current time before INSERT if not already set.
 *
 * HOW TO MODIFY:
 *   - To store additional submission metadata (e.g., grade, reviewer notes), add fields here
 *     and expose them via TaskApiController.
 *   - File storage root is configured by `file.upload-dir` in application.properties;
 *     all submissions go under the `submissions/` sub-directory.
 */
package com.example.demo.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "task_submissions")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TaskSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Employee employee;

    @Column(columnDefinition = "TEXT")
    private String textContent;

    private String fileName;

    private String filePath;

    private LocalDateTime submittedAt;

    @PrePersist
    public void prePersist() {
        if (submittedAt == null) submittedAt = LocalDateTime.now();
    }
}
