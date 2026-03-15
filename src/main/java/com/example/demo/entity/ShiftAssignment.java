/*
 * FILE: ShiftAssignment.java
 * PURPOSE: JPA entity linking a specific employee to a specific shift on a specific date.
 *          Maps to `shift_assignments`. Represents the schedule for a single day.
 *
 * KEY FIELDS:
 *   - employee       : The assigned employee.
 *   - shift          : The shift being assigned.
 *   - assignmentDate : The calendar date of the assignment.
 *   - notes          : Optional notes for this assignment.
 *
 * HOW TO MODIFY:
 *   - To look up the shift for a given day, use ShiftAssignmentRepository
 *     .findByAssignmentDate(date).
 *   - Bulk assignment logic lives in ShiftService.assignShift(); extend that if you need
 *     recurring or template-based scheduling.
 */
package com.example.demo.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "shift_assignments")
public class ShiftAssignment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @ManyToOne
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;
    
    @Column(nullable = false)
    private LocalDate assignmentDate;
    
    private String notes;
}
