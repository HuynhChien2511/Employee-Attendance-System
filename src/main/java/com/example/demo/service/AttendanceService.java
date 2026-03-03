package com.example.demo.service;

import com.example.demo.entity.AttendanceRecord;
import com.example.demo.entity.Employee;
import com.example.demo.repository.AttendanceRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
            AttendanceRecord record = new AttendanceRecord();
            record.setEmployee(employee.get());
            record.setCheckInTime(LocalDateTime.now());
            record.setStatus(AttendanceRecord.AttendanceStatus.PRESENT);
            return attendanceRepository.save(record);
        }
        return null;
    }
    
    public AttendanceRecord checkOut(Long recordId) {
        Optional<AttendanceRecord> record = attendanceRepository.findById(recordId);
        if (record.isPresent()) {
            AttendanceRecord attendance = record.get();
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
