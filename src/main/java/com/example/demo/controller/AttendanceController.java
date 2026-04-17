/*
 * FILE: AttendanceController.java
 * PURPOSE: Legacy REST controller for attendance records at /api/attendance.
 *          Provides basic CRUD-style access without session/role checks.
 *          NOTE: The primary employee check-in/check-out flow is handled by
 *          EmployeeApiController (/api/employee/checkin, /checkout) which
 *          includes session validation and duplicate-check logic.
 *
 * ENDPOINTS:
 *  - GET /api/attendance
 *      Returns all attendance records for all employees (no filtering).
 *
 *  - GET /api/attendance/employee/{employeeId}
 *      Returns all attendance records for a specific employee by their DB ID.
 *
 *  - POST /api/attendance/checkin/{employeeId}
 *      Creates a new check-in record for the specified employee ID.
 *      Returns 400 if employee not found.
 *
 *  - PUT /api/attendance/checkout/{recordId}
 *      Sets check-out time on an existing attendance record by its record ID.
 *      Returns 400 if record not found.
 *
 *  - POST /api/attendance
 *      Creates or saves an arbitrary AttendanceRecord body from the request.
 *      Used for manual attendance entry/corrections.
 *
 * HOW TO MODIFY:
 *  - To add authentication/authorization: inject HttpSession and check
 *    session.getAttribute("userRole") before each handler, similar to
 *    AdminApiController or EmployeeApiController.
 *  - To add date-range filtering: add @RequestParam LocalDate from, LocalDate to
 *    and call AttendanceRecordRepository.findByEmployee_IdAndCheckInTimeBetween().
 *  - To deprecate this controller in favor of the session-aware endpoints:
 *    remove the @CrossOrigin and add role guards, or delete it and route all
 *    attendance operations through EmployeeApiController / AdminApiController.
 */
package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.AttendanceRecord;
import com.example.demo.service.AttendanceService;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "*")
public class AttendanceController {
    
    @Autowired
    private AttendanceService attendanceService;
    
    @GetMapping
    public List<AttendanceRecord> getAllAttendance() {
        return attendanceService.getAllAttendanceRecords();
    }
    
    @GetMapping("/employee/{employeeId}")
    public List<AttendanceRecord> getAttendanceByEmployee(@PathVariable Long employeeId) {
        return attendanceService.getAttendanceByEmployee(employeeId);
    }
    
    @PostMapping("/checkin/{employeeId}")
    public ResponseEntity<AttendanceRecord> checkIn(@PathVariable Long employeeId) {
        AttendanceRecord record = attendanceService.checkIn(employeeId);
        return record != null ? ResponseEntity.ok(record) : ResponseEntity.badRequest().build();
    }
    
    @PostMapping("/checkout/{employeeId}")
    public ResponseEntity<AttendanceRecord> checkOut(@PathVariable Long employeeId) {
        AttendanceRecord record = attendanceService.checkOutByEmployee(employeeId);
        return record != null ? ResponseEntity.ok(record) : ResponseEntity.badRequest().body("No active check-in found for this employee.");
    }
    
    @PostMapping
    public AttendanceRecord createAttendance(@RequestBody AttendanceRecord record) {
        return attendanceService.saveAttendance(record);
    }
}
