/*
 * FILE: AttendanceRecordRepository.java
 * PURPOSE: Spring Data JPA repository for AttendanceRecord.
 *          Provides date-range queries and active-check-in lookups used by the
 *          check-in/out workflow and monthly summary calculations.
 *
 * METHODS:
 *   - findByEmployee(employee)                    : All records for an employee (no date filter).
 *   - findByEmployeeAndCheckInTimeBetween(...)    : Records for an employee within a time range.
 *   - findByEmployee_IdAndCheckInTimeBetweenOrderByCheckInTimeDesc(empId, start, end)
 *       Primary query for attendance history and today-status checks (keyed by employee ID).
 *   - findTopByEmployee_IdAndCheckOutTimeIsNullOrderByCheckInTimeDesc(empId)
 *       Finds the employee's currently active check-in (no checkout yet). Used in checkout.
 *   - findTopByEmployee_IdOrderByCheckInTimeDesc(empId)
 *       Finds the latest check-in regardless of checkout status.
 *
 * HOW TO MODIFY:
 *   - To add a date-only query (using LocalDate instead of LocalDateTime), add a new
 *     method using @Query with CAST or date functions appropriate for your DB dialect.
 */
package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.AttendanceRecord;
import com.example.demo.entity.Employee;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    List<AttendanceRecord> findByEmployee(Employee employee);
    List<AttendanceRecord> findByEmployeeAndCheckInTimeBetween(Employee employee, LocalDateTime start, LocalDateTime end);
    List<AttendanceRecord> findByEmployee_IdAndCheckInTimeBetweenOrderByCheckInTimeDesc(Long employeeId, LocalDateTime start, LocalDateTime end);
    Optional<AttendanceRecord> findTopByEmployee_IdAndCheckOutTimeIsNullOrderByCheckInTimeDesc(Long employeeId);
    Optional<AttendanceRecord> findTopByEmployee_IdOrderByCheckInTimeDesc(Long employeeId);
}
