package com.example.demo.controller;

import com.example.demo.entity.AttendanceRecord;
import com.example.demo.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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
    
    @PutMapping("/checkout/{recordId}")
    public ResponseEntity<AttendanceRecord> checkOut(@PathVariable Long recordId) {
        AttendanceRecord record = attendanceService.checkOut(recordId);
        return record != null ? ResponseEntity.ok(record) : ResponseEntity.badRequest().build();
    }
    
    @PostMapping
    public AttendanceRecord createAttendance(@RequestBody AttendanceRecord record) {
        return attendanceService.saveAttendance(record);
    }
}
