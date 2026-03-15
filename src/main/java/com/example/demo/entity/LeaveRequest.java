/*
 * FILE: LeaveRequest.java
 * PURPOSE: JPA entity representing an employee's leave request. Maps to `leave_requests`.
 *
 * KEY FIELDS:
 *   - leaveType        : SICK_LEAVE | VACATION | PERSONAL | MATERNITY | PATERNITY | UNPAID.
 *   - startDate / endDate : Inclusive range of the leave.
 *   - status           : PENDING | APPROVED | REJECTED | CANCELLED. Defaults to PENDING.
 *   - approverComments : Notes written by the approver when approving or rejecting.
 *
 * HOW TO MODIFY:
 *   - To add a new leave type: extend the LeaveType enum here.
 *   - Approval/rejection logic is in LeaveService.approveLeaveRequest() /
 *     rejectLeaveRequest() and also handled directly in ManagerApiController and
 *     AdminApiController — update all three places if the workflow changes.
 *   - Leave cancellation by the employee (setting status=CANCELLED) is handled in
 *     EmployeeApiController; add validation there if you need to restrict cancellation
 *     windows.
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
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "leave_requests")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class LeaveRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveType leaveType;
    
    @Column(nullable = false)
    private LocalDate startDate;
    
    @Column(nullable = false)
    private LocalDate endDate;
    
    private String reason;
    
    @Enumerated(EnumType.STRING)
    private LeaveStatus status = LeaveStatus.PENDING;
    
    private LocalDateTime requestDate;
    
    private LocalDateTime approvalDate;
    
    private String approverComments;
    
    public enum LeaveType {
        SICK_LEAVE, VACATION, PERSONAL, MATERNITY, PATERNITY, UNPAID
    }
    
    public enum LeaveStatus {
        PENDING, APPROVED, REJECTED, CANCELLED
    }
}
