/*
 * FILE: AttendanceService.java
 * PURPOSE: Business logic layer for employee attendance records.
 *          Manages check-in/check-out operations and general attendance data access.
 *          Works with AttendanceRecordRepository and delegates employee lookups
 *          to EmployeeService.
 *
 * METHODS:
 *  - getAllAttendanceRecords()
 *      Returns every attendance record in the database (no filter).
 *      Used by admin endpoints for the full attendance report view.
 *
 *  - getAttendanceByEmployee(employeeId)
 *      Returns all attendance records for a single employee.
 *      Called from EmployeeApiController and AttendanceController.
 *
 *  - checkIn(employeeId)
 *      Creates a new AttendanceRecord with checkInTime = now() and status PRESENT.
 *      Returns null if the employee ID is not found.
 *      Called from the employee check-in endpoint (POST /api/employee/checkin).
 *
 *  - checkOut(recordId)
 *      Finds an existing record by its ID, sets checkOutTime = now(), then calls
 *      record.calculateHoursWorked() before saving. Returns null if not found.
 *      Called from the employee check-out endpoint (POST /api/employee/checkout).
 *
 *  - saveAttendance(record)
 *      General-purpose save: calculates hours worked if both timestamps are present,
 *      then persists the record. Used by admin/manager manual edits and re-check-in
 *      approvals.
 *
 * HOW TO MODIFY:
 *  - To prevent double check-in: query for an existing open record (no checkOutTime)
 *    for today before creating a new one inside checkIn().
 *  - To change attendance status logic (e.g., auto-flag LATE): add time comparison
 *    in checkIn() against the employee's assigned shift start time.
 *  - To add attendance filtering by date range: add a new method that calls
 *    AttendanceRecordRepository.findByEmployee_IdAndCheckInTimeBetween(...).
 */
package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.AttendanceRecord;
import com.example.demo.entity.Employee;
import com.example.demo.repository.AttendanceRecordRepository;

@Service
public class AttendanceService {
    
    @Autowired
    private AttendanceRecordRepository attendanceRepository;
    
    @Autowired
    private EmployeeService employeeService;
    
    public List<AttendanceRecord> getAllAttendanceRecords() {
        return attendanceRepository.findAll();
    }
    
    public List<AttendanceRecord> getAttendanceByEmployee(Long employeeId) {
        Optional<Employee> employee = employeeService.getEmployeeById(employeeId);
        return employee.map(attendanceRepository::findByEmployee).orElse(List.of());
    }
    
    public AttendanceRecord checkIn(Long employeeId) {
        Optional<Employee> employee = employeeService.getEmployeeById(employeeId);
        if (employee.isPresent()) {
            // Kiểm tra đã check-in chưa checkout trong ngày chưa
            Optional<AttendanceRecord> openRecord = attendanceRepository.findTopByEmployee_IdAndCheckOutTimeIsNullOrderByCheckInTimeDesc(employeeId);
            if (openRecord.isPresent()) {
                // Đã check-in mà chưa check-out
                return null;
            }
            AttendanceRecord record = new AttendanceRecord();
            record.setEmployee(employee.get());
            record.setCheckInTime(LocalDateTime.now());
            record.setStatus(AttendanceRecord.AttendanceStatus.PRESENT);
            return attendanceRepository.save(record);
        }
        return null;
    }
    
    // Không dùng nữa, thay bằng checkOutByEmployee
    // public AttendanceRecord checkOut(Long recordId) { ... }

    public AttendanceRecord checkOutByEmployee(Long employeeId) {
        Optional<AttendanceRecord> openRecord = attendanceRepository.findTopByEmployee_IdAndCheckOutTimeIsNullOrderByCheckInTimeDesc(employeeId);
        if (openRecord.isPresent()) {
            AttendanceRecord attendance = openRecord.get();
            attendance.setCheckOutTime(LocalDateTime.now());
            attendance.calculateHoursWorked();
            return attendanceRepository.save(attendance);
        }
        return null;
    }
    
    public AttendanceRecord saveAttendance(AttendanceRecord record) {
        if (record.getCheckInTime() != null && record.getCheckOutTime() != null) {
            record.calculateHoursWorked();
        }
        return attendanceRepository.save(record);
    }
}
