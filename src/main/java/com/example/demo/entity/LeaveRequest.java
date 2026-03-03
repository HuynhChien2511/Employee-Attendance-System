package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "leave_requests")
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
