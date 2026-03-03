package com.example.demo.service;

import com.example.demo.entity.Shift;
import com.example.demo.entity.ShiftAssignment;
import com.example.demo.entity.Employee;
import com.example.demo.repository.ShiftRepository;
import com.example.demo.repository.ShiftAssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
