/*
 * FILE: Shift.java
 * PURPOSE: JPA entity representing a work shift definition (e.g., "Morning Shift 08:00-16:30").
 *          Maps to the `shifts` table.
 *
 * KEY FIELDS:
 *   - shiftName  : Display name of the shift.
 *   - startTime  : LocalTime (e.g., 08:00).
 *   - endTime    : LocalTime (e.g., 16:30).
 *   - isActive   : false means the shift is retired and won't appear in active shift lists.
 *
 * HOW TO MODIFY:
 *   - To deactivate a shift without deleting historical assignments, set isActive = false
 *     instead of deleting the row.
 *   - Shift definitions are seeded in DataInitializer.java; add new shifts there so they
 *     are present from a fresh database.
 */
package com.example.demo.entity;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "shifts")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Shift {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String shiftName;
    
    @Column(nullable = false)
    private LocalTime startTime;
    
    @Column(nullable = false)
    private LocalTime endTime;
    
    private String description;
    
    private Boolean isActive = true;
}
