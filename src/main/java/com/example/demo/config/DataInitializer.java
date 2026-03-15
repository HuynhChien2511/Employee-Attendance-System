/*
 * FILE: DataInitializer.java
 * PURPOSE: Startup seed configuration that inserts demo data into the database on
 *          first run. Creates shifts, employees, users, attendance history, leave
 *          requests, bonus/penalty records, announcements, and current-day shift assignments.
 *          Seeding is skipped if the admin user already exists.
 *
 * METHODS:
 *  - initDatabase(...repositories)
 *      Declares a CommandLineRunner bean executed at application startup.
 *      Inside the runner:
 *        1. Checks for existing admin user and exits if found.
 *        2. Creates default shifts.
 *        3. Creates manager employees and manager user accounts.
 *        4. Creates sample employees and linked employee user accounts.
 *        5. Creates an admin user.
 *        6. Seeds recent attendance records.
 *        7. Seeds example leave requests, bonus/penalty records, announcements,
 *           and initial shift assignments.
 *
 * HOW TO MODIFY:
 *  - To change demo credentials or sample staff: edit the hard-coded values in the
 *    runner body.
 *  - To disable seeding in production: guard the bean with a Spring profile such as
 *    @Profile("dev") or read a feature flag from application.properties.
 *  - To add new demo entities (tasks, notifications, suggestions): inject the extra
 *    repositories into initDatabase() and append the seed logic after user creation.
 */
package com.example.demo.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.demo.entity.Announcement;
import com.example.demo.entity.AttendanceRecord;
import com.example.demo.entity.BonusPenalty;
import com.example.demo.entity.Employee;
import com.example.demo.entity.LeaveRequest;
import com.example.demo.entity.Shift;
import com.example.demo.entity.ShiftAssignment;
import com.example.demo.entity.User;
import com.example.demo.repository.AnnouncementRepository;
import com.example.demo.repository.AttendanceRecordRepository;
import com.example.demo.repository.BonusPenaltyRepository;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.LeaveRequestRepository;
import com.example.demo.repository.ShiftAssignmentRepository;
import com.example.demo.repository.ShiftRepository;
import com.example.demo.repository.UserRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(
            EmployeeRepository employeeRepo,
            UserRepository userRepo,
            AttendanceRecordRepository attendanceRepo,
            ShiftRepository shiftRepo,
            ShiftAssignmentRepository shiftAssignmentRepo,
            LeaveRequestRepository leaveRepo,
            BonusPenaltyRepository bonusPenaltyRepo,
            AnnouncementRepository announcementRepo) {

        return args -> {
            // ── Only seed if database is empty (first-time setup) ──
            if (userRepo.findByUsername("admin").isPresent()) {
                System.out.println("ℹ️  Database already seeded. Skipping initialization.");
                return;
            }

            System.out.println("🌱 Seeding initial data...");

            // ── Shifts ──────────────────────────────────────────────
            Shift morning = new Shift();
            morning.setShiftName("Morning Shift");
            morning.setStartTime(LocalTime.of(8, 0));
            morning.setEndTime(LocalTime.of(16, 30));
            morning.setDescription("Standard morning shift 08:00 - 16:30");
            morning.setIsActive(true);
            shiftRepo.save(morning);

            Shift afternoon = new Shift();
            afternoon.setShiftName("Afternoon Shift");
            afternoon.setStartTime(LocalTime.of(13, 0));
            afternoon.setEndTime(LocalTime.of(21, 0));
            afternoon.setDescription("Afternoon shift 13:00 - 21:00");
            afternoon.setIsActive(true);
            shiftRepo.save(afternoon);

            // ── Manager Employees ─────────────────────────────────
            Employee empMgr1 = new Employee();
            empMgr1.setFirstName("Nguyen");
            empMgr1.setLastName("Van Manager");
            empMgr1.setEmail("manager1@company.com");
            empMgr1.setEmployeeId("MGR001");
            empMgr1.setPhone("0900000001");
            empMgr1.setDepartment("Engineering");
            empMgr1.setPosition("Engineering Manager");
            empMgr1.setHireDate(LocalDate.of(2020, 1, 10));
            empMgr1.setStatus(Employee.EmployeeStatus.ACTIVE);
            empMgr1.setBaseSalary(new BigDecimal("25000000"));
            employeeRepo.save(empMgr1);

            Employee empMgr2 = new Employee();
            empMgr2.setFirstName("Tran");
            empMgr2.setLastName("Thi Manager");
            empMgr2.setEmail("manager2@company.com");
            empMgr2.setEmployeeId("MGR002");
            empMgr2.setPhone("0900000002");
            empMgr2.setDepartment("Sales");
            empMgr2.setPosition("Sales Manager");
            empMgr2.setHireDate(LocalDate.of(2020, 3, 15));
            empMgr2.setStatus(Employee.EmployeeStatus.ACTIVE);
            empMgr2.setBaseSalary(new BigDecimal("22000000"));
            employeeRepo.save(empMgr2);

            // ── Manager Users ──────────────────────────────────────
            User mgr1User = new User();
            mgr1User.setUsername("manager1");
            mgr1User.setPassword("Manager@123");
            mgr1User.setRole(User.UserRole.MANAGER);
            mgr1User.setEmployee(empMgr1);
            mgr1User.setIsActive(true);
            userRepo.save(mgr1User);

            User mgr2User = new User();
            mgr2User.setUsername("manager2");
            mgr2User.setPassword("Manager@123");
            mgr2User.setRole(User.UserRole.MANAGER);
            mgr2User.setEmployee(empMgr2);
            mgr2User.setIsActive(true);
            userRepo.save(mgr2User);

            // ── Regular Employees ──────────────────────────────────
            String[][] empData = {
                {"Le", "Van A",    "emp001@company.com", "EMP001", "0900100001", "Engineering", "Junior Dev",  "2022-06-01", "12000000"},
                {"Pham", "Thi B",  "emp002@company.com", "EMP002", "0900100002", "Engineering", "Senior Dev", "2021-09-15", "18000000"},
                {"Hoang", "Van C", "emp003@company.com", "EMP003", "0900100003", "Engineering", "DevOps",     "2023-02-20", "15000000"},
                {"Vo", "Thi D",    "emp004@company.com", "EMP004", "0900100004", "Sales",       "Sales Rep",  "2022-11-10", "11000000"},
                {"Dang", "Van E",  "emp005@company.com", "EMP005", "0900100005", "Sales",       "Sales Lead", "2021-05-03", "14000000"},
            };

            Employee[] employees = new Employee[empData.length];
            for (int i = 0; i < empData.length; i++) {
                String[] d = empData[i];
                Employee emp = new Employee();
                emp.setFirstName(d[0]);
                emp.setLastName(d[1]);
                emp.setEmail(d[2]);
                emp.setEmployeeId(d[3]);
                emp.setPhone(d[4]);
                emp.setDepartment(d[5]);
                emp.setPosition(d[6]);
                emp.setHireDate(LocalDate.parse(d[7]));
                emp.setStatus(Employee.EmployeeStatus.ACTIVE);
                emp.setBaseSalary(new BigDecimal(d[8]));
                emp.setManagerUserId("Engineering".equals(d[5]) ? mgr1User.getId() : mgr2User.getId());
                employees[i] = employeeRepo.save(emp);
            }

            // ── Employee User Accounts ─────────────────────────────
            String[] empUsernames = {"emp001", "emp002", "emp003", "emp004", "emp005"};
            for (int i = 0; i < employees.length; i++) {
                User u = new User();
                u.setUsername(empUsernames[i]);
                u.setPassword("Employee@123");
                u.setRole(User.UserRole.EMPLOYEE);
                u.setEmployee(employees[i]);
                u.setIsActive(true);
                userRepo.save(u);
            }

            // ── Admin User ─────────────────────────────────────────
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("Admin@123");
            admin.setRole(User.UserRole.ADMIN);
            admin.setIsActive(true);
            userRepo.save(admin);

            // ── Attendance Records (last 20 working days) ──────────
            for (int dayOffset = 1; dayOffset <= 20; dayOffset++) {
                LocalDate workDay = LocalDate.now().minusDays(dayOffset);
                if (workDay.getDayOfWeek().getValue() >= 6) continue;
                for (int ei = 0; ei < 3; ei++) {
                    AttendanceRecord rec = new AttendanceRecord();
                    rec.setEmployee(employees[ei]);
                    LocalDateTime checkIn = workDay.atTime(8, dayOffset % 3 == 0 ? 15 : 0, 0);
                    LocalDateTime checkOut = workDay.atTime(17, 0, 0);
                    rec.setCheckInTime(checkIn);
                    rec.setCheckOutTime(checkOut);
                    rec.calculateHoursWorked();
                    rec.setStatus(checkIn.getMinute() == 0
                            ? AttendanceRecord.AttendanceStatus.PRESENT
                            : AttendanceRecord.AttendanceStatus.LATE);
                    attendanceRepo.save(rec);
                }
            }

            // ── Leave Requests ─────────────────────────────────────
            LeaveRequest leave1 = new LeaveRequest();
            leave1.setEmployee(employees[0]);
            leave1.setLeaveType(LeaveRequest.LeaveType.SICK_LEAVE);
            leave1.setStartDate(LocalDate.now().plusDays(3));
            leave1.setEndDate(LocalDate.now().plusDays(4));
            leave1.setReason("Medical appointment");
            leave1.setStatus(LeaveRequest.LeaveStatus.PENDING);
            leave1.setRequestDate(LocalDateTime.now().minusDays(1));
            leaveRepo.save(leave1);

            LeaveRequest leave2 = new LeaveRequest();
            leave2.setEmployee(employees[1]);
            leave2.setLeaveType(LeaveRequest.LeaveType.VACATION);
            leave2.setStartDate(LocalDate.now().plusDays(10));
            leave2.setEndDate(LocalDate.now().plusDays(14));
            leave2.setReason("Annual leave");
            leave2.setStatus(LeaveRequest.LeaveStatus.PENDING);
            leave2.setRequestDate(LocalDateTime.now().minusDays(2));
            leaveRepo.save(leave2);

            // ── Bonus / Penalty ────────────────────────────────────
            int thisMonth = LocalDate.now().getMonthValue();
            int thisYear = LocalDate.now().getYear();

            BonusPenalty bonus1 = new BonusPenalty();
            bonus1.setEmployee(employees[0]);
            bonus1.setType(BonusPenalty.RecordType.BONUS);
            bonus1.setAmount(new BigDecimal("2000000"));
            bonus1.setReason("Outstanding performance");
            bonus1.setMonth(thisMonth);
            bonus1.setYear(thisYear);
            bonus1.setEffectiveDate(LocalDate.now());
            bonus1.setCreatedBy(admin);
            bonusPenaltyRepo.save(bonus1);

            BonusPenalty penalty1 = new BonusPenalty();
            penalty1.setEmployee(employees[2]);
            penalty1.setType(BonusPenalty.RecordType.PENALTY);
            penalty1.setAmount(new BigDecimal("500000"));
            penalty1.setReason("Multiple late arrivals");
            penalty1.setMonth(thisMonth);
            penalty1.setYear(thisYear);
            penalty1.setEffectiveDate(LocalDate.now());
            penalty1.setCreatedBy(admin);
            bonusPenaltyRepo.save(penalty1);

            // ── Announcement ───────────────────────────────────────
            Announcement ann = new Announcement();
            ann.setSender(admin);
            ann.setTargetType(Announcement.TargetType.ALL);
            ann.setTitle("Welcome to Employee Attendance System");
            ann.setContent("Dear all, welcome to the new EAS. Default passwords — Manager: Manager@123 | Employee: Employee@123. Please change after first login.");
            announcementRepo.save(ann);

            // ── Shift Assignments ──────────────────────────────────
            for (Employee emp : employees) {
                ShiftAssignment sa = new ShiftAssignment();
                sa.setEmployee(emp);
                sa.setShift(morning);
                sa.setAssignmentDate(LocalDate.now());
                shiftAssignmentRepo.save(sa);
            }

            System.out.println("✅ Database seeded successfully!");
            System.out.println("   admin    / Admin@123");
            System.out.println("   manager1 / Manager@123");
            System.out.println("   emp001   / Employee@123");
        };
    }
}
