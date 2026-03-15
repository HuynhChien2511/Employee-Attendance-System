/*
 * FILE: ShiftAssignmentRepository.java
 * PURPOSE: Spring Data JPA repository for ShiftAssignment.
 *          Links employees to shifts on specific calendar dates.
 *
 * METHODS:
 *   - findByEmployee(employee)          : All assignments for a given Employee object.
 *   - findByAssignmentDate(date)        : All assignments on a specific date (useful for
 *                                         daily roster views).
 *
 * HOW TO MODIFY:
 *   - To query assignments within a date range, add:
 *     findByEmployee_IdAndAssignmentDateBetween(Long empId, LocalDate start, LocalDate end)
 *   - To look up an employee's shift for today, call findByAssignmentDate(LocalDate.now())
 *     and filter by employee.
 */
package com.example.demo.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Employee;
import com.example.demo.entity.ShiftAssignment;

@Repository
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, Long> {
    List<ShiftAssignment> findByEmployee(Employee employee);
    List<ShiftAssignment> findByAssignmentDate(LocalDate date);
}
