/*
 * FILE: ShiftService.java
 * PURPOSE: Business logic for work shifts and their assignments to employees.
 *          Manages Shift definitions (name, start/end time) and ShiftAssignment
 *          records that link a shift to an employee on a specific date.
 *
 * METHODS:
 *  - getAllShifts()
 *      Returns all shift definitions regardless of active status.
 *      Used in admin management screens.
 *
 *  - getActiveShifts()
 *      Returns only shifts where isActive = true.
 *      Used in dropdowns when assigning shifts to employees.
 *
 *  - saveShift(shift)
 *      Persists a new or updated Shift definition.
 *      Called from admin POST/PUT shift endpoints.
 *
 *  - getShiftAssignmentsByEmployee(employeeId)
 *      Returns all shift assignments for a given employee.
 *      Used on the employee's schedule view.
 *
 *  - getShiftAssignmentsByDate(date)
 *      Returns all shift assignments for a specific calendar date across all employees.
 *      Used by admin/manager to see the daily roster.
 *
 *  - assignShift(assignment)
 *      Creates or updates a ShiftAssignment record (employee + shift + date).
 *      Called from POST /api/admin/shift-assignments.
 *
 * HOW TO MODIFY:
 *  - To prevent conflicting assignments (duplicate shift on same date for one employee):
 *    add a uniqueness check in assignShift() before saving.
 *  - To deactivate a shift without deleting: set isActive = false via saveShift()
 *    rather than deleting; this preserves historical assignment references.
 *  - To add weekly recurring schedules: add a Schedule entity that generates
 *    ShiftAssignment records for a date range, then call assignShift() in a loop.
 */
package com.example.demo.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Employee;
import com.example.demo.entity.Shift;
import com.example.demo.entity.ShiftAssignment;
import com.example.demo.repository.ShiftAssignmentRepository;
import com.example.demo.repository.ShiftRepository;

@Service
public class ShiftService {
    
    @Autowired
    private ShiftRepository shiftRepository;
    
    @Autowired
    private ShiftAssignmentRepository shiftAssignmentRepository;
    
    @Autowired
    private EmployeeService employeeService;
    
    public List<Shift> getAllShifts() {
        return shiftRepository.findAll();
    }
    
    public List<Shift> getActiveShifts() {
        return shiftRepository.findByIsActive(true);
    }
    
    public Shift saveShift(Shift shift) {
        return shiftRepository.save(shift);
    }
    
    public List<ShiftAssignment> getShiftAssignmentsByEmployee(Long employeeId) {
        Optional<Employee> employee = employeeService.getEmployeeById(employeeId);
        return employee.map(shiftAssignmentRepository::findByEmployee).orElse(List.of());
    }
    
    public List<ShiftAssignment> getShiftAssignmentsByDate(LocalDate date) {
        return shiftAssignmentRepository.findByAssignmentDate(date);
    }
    
    public ShiftAssignment assignShift(ShiftAssignment assignment) {
        return shiftAssignmentRepository.save(assignment);
    }
}
