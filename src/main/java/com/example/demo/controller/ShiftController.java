package com.example.demo.controller;

import com.example.demo.entity.Shift;
import com.example.demo.entity.ShiftAssignment;
import com.example.demo.service.ShiftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

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
