/*
 * FILE: AttendanceRecord.java
 * PURPOSE: JPA entity representing a single check-in / check-out event for an employee.
 *          Maps to the `attendance_records` table.
 *
 * KEY FIELDS:
 *   - checkInTime  : When the employee checked in (required).
 *   - checkOutTime : When the employee checked out; null means still checked in.
 *   - hoursWorked  : Computed from checkIn/checkOut; set via calculateHoursWorked().
 *   - status       : PRESENT | ABSENT | LATE | HALF_DAY | ON_LEAVE.
 *   - notes        : Optional free-text notes (admin/manager can set).
 *
 * KEY METHOD:
 *   - calculateHoursWorked() : Computes the duration from checkInTime to checkOutTime in hours.
 *                              Call this before saving after checkout.
 *
 * HOW TO MODIFY:
 *   - Late-threshold logic (>=9:00 → LATE) lives in EmployeeApiController.checkIn(); change
 *     the hour check there to adjust the late cutoff time.
 *   - HALF_DAY threshold (<4 h) lives in EmployeeApiController.checkOut(); adjust similarly.
 *   - To add geolocation or device data, add fields here and capture them in the check-in
 *     endpoint.
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
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "attendance_records")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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
