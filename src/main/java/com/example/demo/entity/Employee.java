/*
 * FILE: Employee.java
 * PURPOSE: JPA entity representing an employee record. Maps to the `employees` table.
 *          Each employee can be linked to exactly one User account (one-to-one via User.employee).
 *
 * KEY FIELDS:
 *   - employeeId   : Human-readable code (e.g., "EMP001"), unique.
 *   - department   : Department name used to scope announcements and attendance reports.
 *   - managerUserId: FK to the User.id of the employee's manager; used for approval workflows.
 *   - baseSalary   : Monthly base salary used in MonthlySummaryService calculations.
 *   - status       : ACTIVE | INACTIVE | ON_LEAVE.
 *
 * HOW TO MODIFY:
 *   - Adding a field: add it here, then add the column in the DB (or rely on ddl-auto=update),
 *     and update DataInitializer.java seed data if needed.
 *   - Changing salary logic: baseSalary is read directly in MonthlySummaryService.compute();
 *     update that method if salary structure changes.
 *   - To add a new status: extend the EmployeeStatus enum and handle it in any switch/filter
 *     expressions in AdminApiController and MonthlySummaryService.
 */
package com.example.demo.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "employees")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Employee {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String firstName;
    
    @Column(nullable = false)
    private String lastName;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(unique = true, nullable = false)
    private String employeeId;
    
    private String phone;
    
    private String department;
    
    private String position;
    
    private LocalDate hireDate;
    
    @Enumerated(EnumType.STRING)
    private EmployeeStatus status;

    private Long managerUserId;
    private BigDecimal baseSalary = BigDecimal.ZERO;

    public enum EmployeeStatus {
        ACTIVE, INACTIVE, ON_LEAVE
    }
}
