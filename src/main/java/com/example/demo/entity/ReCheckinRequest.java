/*
 * FILE: ReCheckinRequest.java
 * PURPOSE: JPA entity representing an employee's request to retroactively record a check-in
 *          for a past date. Maps to `re_checkin_requests`.
 *
 * KEY FIELDS:
 *   - requestedDate        : The date the employee claims they were present but forgot to check in.
 *   - requestedCheckinTime : The time the employee requests to be recorded as their check-in.
 *   - attendanceRecord     : Linked after approval; points to the AttendanceRecord that was
 *                            created or updated as a result.
 *   - status               : PENDING | APPROVED | REJECTED.
 *   - approvedBy / approvedAt : Set on approval by the approving user.
 *
 * HOW TO MODIFY:
 *   - Approval logic (creating the attendance record) lives in
 *     ReCheckinRequestService.approve() — modify there to change what happens on approval.
 *   - Managers approve for their own team; admins approve all pending. This routing is
 *     handled in ManagerApiController and AdminApiController respectively.
 */
package com.example.demo.entity;

import java.time.LocalDate;
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
@Table(name = "re_checkin_requests")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ReCheckinRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "attendance_record_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private AttendanceRecord attendanceRecord;

    @Column(nullable = false)
    private LocalDate requestedDate;

    private LocalDateTime requestedCheckinTime;
    private String reason;

    @Enumerated(EnumType.STRING)
    private RequestStatus status = RequestStatus.PENDING;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User approvedBy;

    private LocalDateTime approvedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public enum RequestStatus {
        PENDING, APPROVED, REJECTED
    }
}
