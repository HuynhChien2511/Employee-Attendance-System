package com.example.demo.config;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(
            EmployeeRepository employeeRepo,
            AttendanceRecordRepository attendanceRepo,
            ShiftRepository shiftRepo,
            ShiftAssignmentRepository shiftAssignmentRepo,
            LeaveRequestRepository leaveRepo) {
        
        return args -> {
            // Create sample employees
            Employee emp1 = new Employee();
            emp1.setFirstName("John");
            emp1.setLastName("Doe");
            emp1.setEmail("john.doe@company.com");
            emp1.setEmployeeId("EMP001");
            emp1.setPhone("555-0101");
            emp1.setDepartment("Engineering");
            emp1.setPosition("Software Engineer");
            emp1.setHireDate(LocalDate.of(2023, 1, 15));
            emp1.setStatus(Employee.EmployeeStatus.ACTIVE);
            employeeRepo.save(emp1);

            Employee emp2 = new Employee();
            emp2.setFirstName("Jane");
            emp2.setLastName("Smith");
            emp2.setEmail("jane.smith@company.com");
            emp2.setEmployeeId("EMP002");
            emp2.setPhone("555-0102");
            emp2.setDepartment("Human Resources");
            emp2.setPosition("HR Manager");
            emp2.setHireDate(LocalDate.of(2022, 6, 1));
            emp2.setStatus(Employee.EmployeeStatus.ACTIVE);
            employeeRepo.save(emp2);

            Employee emp3 = new Employee();
            emp3.setFirstName("Mike");
            emp3.setLastName("Johnson");
            emp3.setEmail("mike.johnson@company.com");
            emp3.setEmployeeId("EMP003");
            emp3.setPhone("555-0103");
            emp3.setDepartment("Sales");
            emp3.setPosition("Sales Representative");
            emp3.setHireDate(LocalDate.of(2023, 3, 20));
            emp3.setStatus(Employee.EmployeeStatus.ACTIVE);
            employeeRepo.save(emp3);

            // Create sample shifts
            Shift morningShift = new Shift();
            morningShift.setShiftName("Morning Shift");
            morningShift.setStartTime(LocalTime.of(8, 0));
            morningShift.setEndTime(LocalTime.of(16, 0));
            morningShift.setDescription("Regular morning shift");
            morningShift.setIsActive(true);
            shiftRepo.save(morningShift);

            Shift eveningShift = new Shift();
            eveningShift.setShiftName("Evening Shift");
            eveningShift.setStartTime(LocalTime.of(16, 0));
            eveningShift.setEndTime(LocalTime.of(0, 0));
            eveningShift.setDescription("Regular evening shift");
            eveningShift.setIsActive(true);
            shiftRepo.save(eveningShift);

            Shift nightShift = new Shift();
            nightShift.setShiftName("Night Shift");
            nightShift.setStartTime(LocalTime.of(0, 0));
            nightShift.setEndTime(LocalTime.of(8, 0));
            nightShift.setDescription("Regular night shift");
            nightShift.setIsActive(true);
            shiftRepo.save(nightShift);

            // Create sample attendance records
            AttendanceRecord attendance1 = new AttendanceRecord();
            attendance1.setEmployee(emp1);
            attendance1.setCheckInTime(LocalDateTime.now().minusHours(5));
            attendance1.setCheckOutTime(LocalDateTime.now().minusHours(1));
            attendance1.setStatus(AttendanceRecord.AttendanceStatus.PRESENT);
            attendance1.calculateHoursWorked();
            attendanceRepo.save(attendance1);

            AttendanceRecord attendance2 = new AttendanceRecord();
            attendance2.setEmployee(emp2);
            attendance2.setCheckInTime(LocalDateTime.now().minusHours(4));
            attendance2.setStatus(AttendanceRecord.AttendanceStatus.PRESENT);
            attendanceRepo.save(attendance2);

            // Create sample shift assignments
            ShiftAssignment assignment1 = new ShiftAssignment();
            assignment1.setEmployee(emp1);
            assignment1.setShift(morningShift);
            assignment1.setAssignmentDate(LocalDate.now());
            assignment1.setNotes("Regular assignment");
            shiftAssignmentRepo.save(assignment1);

            ShiftAssignment assignment2 = new ShiftAssignment();
            assignment2.setEmployee(emp2);
            assignment2.setShift(morningShift);
            assignment2.setAssignmentDate(LocalDate.now());
            shiftAssignmentRepo.save(assignment2);

            // Create sample leave requests
            LeaveRequest leave1 = new LeaveRequest();
            leave1.setEmployee(emp3);
            leave1.setLeaveType(LeaveRequest.LeaveType.VACATION);
            leave1.setStartDate(LocalDate.now().plusDays(10));
            leave1.setEndDate(LocalDate.now().plusDays(15));
            leave1.setReason("Family vacation");
            leave1.setStatus(LeaveRequest.LeaveStatus.PENDING);
            leave1.setRequestDate(LocalDateTime.now());
            leaveRepo.save(leave1);

            LeaveRequest leave2 = new LeaveRequest();
            leave2.setEmployee(emp1);
            leave2.setLeaveType(LeaveRequest.LeaveType.SICK_LEAVE);
            leave2.setStartDate(LocalDate.now().minusDays(2));
            leave2.setEndDate(LocalDate.now().minusDays(1));
            leave2.setReason("Medical appointment");
            leave2.setStatus(LeaveRequest.LeaveStatus.APPROVED);
            leave2.setRequestDate(LocalDateTime.now().minusDays(3));
            leave2.setApprovalDate(LocalDateTime.now().minusDays(2));
            leave2.setApproverComments("Approved by manager");
            leaveRepo.save(leave2);

            System.out.println("✅ Sample data initialized successfully!");
        };
    }
}
