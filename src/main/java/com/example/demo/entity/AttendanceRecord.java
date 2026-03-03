package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "attendance_records")
public class AttendanceRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @Column(nullable = false)
    private LocalDateTime checkInTime;
    
    private LocalDateTime checkOutTime;
    
    private Double hoursWorked;
    
    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;
    
    private String notes;
    
    public enum AttendanceStatus {
        PRESENT, ABSENT, LATE, HALF_DAY, ON_LEAVE
    }
    
    // Calculate hours worked
    public void calculateHoursWorked() {
        if (checkInTime != null && checkOutTime != null) {
            long minutes = java.time.Duration.between(checkInTime, checkOutTime).toMinutes();
            this.hoursWorked = minutes / 60.0;
        }
    }
}
