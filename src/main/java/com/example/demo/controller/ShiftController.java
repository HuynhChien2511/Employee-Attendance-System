/*
 * FILE: ShiftController.java
 * PURPOSE: Legacy REST controller for shift management at /api/shifts.
 *          Provides unauthenticated access to shift definitions and assignments.
 *          NOTE: Shift management in the main UI is performed via AdminApiController
 *          and ManagerApiController which enforce session/role checks.
 *
 * ENDPOINTS:
 *  - GET /api/shifts
 *      Returns all shift definitions regardless of active status.
 *
 *  - GET /api/shifts/active
 *      Returns only active shifts (isActive = true). Used in assignment dropdowns.
 *
 *  - POST /api/shifts
 *      Creates a new shift definition from request body.
 *
 *  - GET /api/shifts/assignments/employee/{employeeId}
 *      Returns all shift assignments for a specific employee.
 *
 *  - GET /api/shifts/assignments/date/{date}
 *      Returns all shift assignments for a specific date (ISO format: yyyy-MM-dd).
 *
 *  - POST /api/shifts/assignments
 *      Creates a new shift assignment (employee + shift + date).
 *
 * HOW TO MODIFY:
 *  - To add role protection: inject HttpSession and check userRole before each
 *    operation, then remove the @CrossOrigin annotation.
 *  - To update or delete shifts: add @PutMapping("/{id}") and @DeleteMapping("/{id}")
 *    handlers that call ShiftService.saveShift() and a new deleteShift() method.
 *  - To prevent duplicate assignments: add logic in the assignShift endpoint to
 *    check for an existing assignment on the same date + employee before saving.
 */
package com.example.demo.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Shift;
import com.example.demo.entity.ShiftAssignment;
import com.example.demo.service.ShiftService;

@RestController
@RequestMapping("/api/shifts")
@CrossOrigin(origins = "*")
public class ShiftController {
    
    @Autowired
    private ShiftService shiftService;
    
    @GetMapping
    public List<Shift> getAllShifts() {
        return shiftService.getAllShifts();
    }
    
    @GetMapping("/active")
    public List<Shift> getActiveShifts() {
        return shiftService.getActiveShifts();
    }
    
    @PostMapping
    public Shift createShift(@RequestBody Shift shift) {
        return shiftService.saveShift(shift);
    }
    
    @GetMapping("/assignments/employee/{employeeId}")
    public List<ShiftAssignment> getShiftAssignmentsByEmployee(@PathVariable Long employeeId) {
        return shiftService.getShiftAssignmentsByEmployee(employeeId);
    }
    
    @GetMapping("/assignments/date/{date}")
    public List<ShiftAssignment> getShiftAssignmentsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return shiftService.getShiftAssignmentsByDate(date);
    }
    
    @PostMapping("/assignments")
    public ShiftAssignment assignShift(@RequestBody ShiftAssignment assignment) {
        return shiftService.assignShift(assignment);
    }
}
