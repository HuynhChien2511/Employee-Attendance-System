/*
 * FILE: AdminApiController.java
 * PURPOSE: Session-authenticated REST controller for all admin operations at /api/admin.
 *          Every endpoint checks isAdmin(session) and returns 403 if the caller is not ADMIN.
 *          Covers employee management, user account management, attendance view,
 *          monthly salary summaries, bonus/penalty records, announcements, and
 *          leave request administration.
 *
 * HELPER METHODS:
 *  - isAdmin(session)     Returns true if session.userRole == "ADMIN".
 *  - forbidden()          Returns 403 response with error message.
 *  - currentUser(session) Resolves User by session userId.
 *
 * ENDPOINTS — EMPLOYEES (/api/admin/employees):
 *  - GET     Returns all employees.
 *  - POST    Creates an employee and optionally a linked user account.
 *            Body: firstName, lastName, email, employeeCode, phone, department,
 *            position, hireDate, baseSalary, managerUserId, username?, password?, role?.
 *  - PUT /{id}   Partial update: only updates fields present in the request body.
 *  - DELETE /{id} Deactivates linked user accounts, then hard-deletes the employee.
 *
 * ENDPOINTS — USERS (/api/admin/users):
 *  - GET     Returns all users with their linked employee info.
 *  - POST    Creates a standalone user account. Can link to an existing employee.
 *  - PUT /{id}  Updates password, role, or isActive flag.
 *  - PUT /{id}/toggle  Toggles the isActive flag on/off.
 *
 * ENDPOINTS — ATTENDANCE (/api/admin/attendance/{employeeId}):
 *  - GET ?days=30  Returns attendance records for the specified employee in the last N days.
 *
 * ENDPOINTS — MONTHLY SUMMARY (/api/admin/monthly-summary/{employeeId}):
 *  - GET ?month=&year=  Delegates to MonthlySummaryService.compute().
 *
 * ENDPOINTS — BONUS/PENALTY (/api/admin/bonus-penalty):
 *  - GET     Returns all bonus/penalty records.
 *  - POST    Creates a record and notifies the employee.
 *  - DELETE /{id}  Deletes the record.
 *
 * ENDPOINTS — ANNOUNCEMENTS (/api/admin/announcements):
 *  - GET     Returns all announcements.
 *  - POST    Creates an announcement with the specified target audience.
 *
 * ENDPOINTS — LEAVE REQUESTS (/api/admin/leave-requests):
 *  - GET     Returns all leave requests.
 *  - PUT /{id}/approve  Approves and notifies the employee.
 *  - PUT /{id}/reject   Rejects (optionally with comment) and notifies the employee.
 *
 * ENDPOINTS — STATS (/api/admin/stats):
 *  - GET  Returns: totalEmployees, presentToday, pendingLeaves, totalUsers.
 *
 * HOW TO MODIFY:
 *  - To add a new admin feature: add the relevant @Autowired service, then a new
 *    endpoint method with the isAdmin() guard at the top.
 *  - To send email on leave decisions: inject a MailService and call it inside
 *    approveLeave() / rejectLeave() after the notification call.
 *  - To paginate the employee or user list: change the return type to
 *    ResponseEntity<Page<?>> and add a Pageable parameter.
 */
package com.example.demo.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Announcement;
import com.example.demo.entity.BonusPenalty;
import com.example.demo.entity.Employee;
import com.example.demo.entity.LeaveRequest;
import com.example.demo.entity.User;
import com.example.demo.repository.AttendanceRecordRepository;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.LeaveRequestRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AnnouncementService;
import com.example.demo.service.BonusPenaltyService;
import com.example.demo.service.MonthlySummaryService;
import com.example.demo.service.NotificationService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/admin")
public class AdminApiController {

    @Autowired private EmployeeRepository employeeRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private AttendanceRecordRepository attendanceRepo;
    @Autowired private LeaveRequestRepository leaveRepo;
    @Autowired private BonusPenaltyService bonusPenaltyService;
    @Autowired private AnnouncementService announcementService;
    @Autowired private MonthlySummaryService monthlySummaryService;
    @Autowired private NotificationService notificationService;

    private ResponseEntity<?> forbidden() {
        return ResponseEntity.status(403).body(Map.of("error", "Admin access required"));
    }

    private boolean isAdmin(HttpSession s) {
        return "ADMIN".equals(s.getAttribute("userRole"));
    }

    private User currentUser(HttpSession s) {
        Long id = (Long) s.getAttribute("userId");
        return id == null ? null : userRepo.findById(id).orElse(null);
    }

    // ─── EMPLOYEES ────────────────────────────────────────────────

    @GetMapping("/employees")
    public ResponseEntity<?> listEmployees(HttpSession session) {
        if (!isAdmin(session)) return forbidden();
        return ResponseEntity.ok(employeeRepo.findAll());
    }

    @PostMapping("/employees")
    public ResponseEntity<?> createEmployee(@RequestBody Map<String, Object> body, HttpSession session) {
        if (!isAdmin(session)) return forbidden();
        Employee emp = new Employee();
        emp.setFirstName((String) body.get("firstName"));
        emp.setLastName((String) body.get("lastName"));
        emp.setEmail((String) body.get("email"));
        emp.setEmployeeId((String) body.get("employeeCode"));
        emp.setPhone((String) body.getOrDefault("phone", ""));
        emp.setDepartment((String) body.getOrDefault("department", ""));
        emp.setPosition((String) body.getOrDefault("position", ""));
        emp.setStatus(Employee.EmployeeStatus.ACTIVE);
        String hireDateStr = (String) body.get("hireDate");
        emp.setHireDate(hireDateStr != null ? LocalDate.parse(hireDateStr) : LocalDate.now());
        Object salary = body.get("baseSalary");
        emp.setBaseSalary(salary != null ? new BigDecimal(salary.toString()) : BigDecimal.ZERO);
        Object mgr = body.get("managerUserId");
        emp.setManagerUserId(mgr != null ? Long.parseLong(mgr.toString()) : null);
        Employee saved = employeeRepo.save(emp);

        // Optionally create user account
        String username = (String) body.get("username");
        String password = (String) body.get("password");
        String roleStr = (String) body.getOrDefault("role", "EMPLOYEE");
        if (username != null && !username.isBlank()) {
            User u = new User();
            u.setUsername(username);
            u.setPassword(password != null ? password : "Password@123");
            u.setRole(User.UserRole.valueOf(roleStr));
            u.setEmployee(saved);
            u.setIsActive(true);
            userRepo.save(u);
        }
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/employees/{id}")
    public ResponseEntity<?> updateEmployee(@PathVariable Long id,
                                            @RequestBody Map<String, Object> body,
                                            HttpSession session) {
        if (!isAdmin(session)) return forbidden();
        return employeeRepo.findById(id).map(emp -> {
            if (body.containsKey("firstName")) emp.setFirstName((String) body.get("firstName"));
            if (body.containsKey("lastName")) emp.setLastName((String) body.get("lastName"));
            if (body.containsKey("email")) emp.setEmail((String) body.get("email"));
            if (body.containsKey("phone")) emp.setPhone((String) body.get("phone"));
            if (body.containsKey("department")) emp.setDepartment((String) body.get("department"));
            if (body.containsKey("position")) emp.setPosition((String) body.get("position"));
            if (body.containsKey("status")) emp.setStatus(Employee.EmployeeStatus.valueOf((String) body.get("status")));
            if (body.containsKey("baseSalary")) emp.setBaseSalary(new BigDecimal(body.get("baseSalary").toString()));
            if (body.containsKey("managerUserId")) {
                Object mgr = body.get("managerUserId");
                emp.setManagerUserId(mgr != null && !mgr.toString().isBlank() ? Long.parseLong(mgr.toString()) : null);
            }
            return ResponseEntity.ok(employeeRepo.save(emp));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/employees/{id}")
    public ResponseEntity<?> deleteEmployee(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) return forbidden();
        if (!employeeRepo.existsById(id)) return ResponseEntity.notFound().build();
        // Deactivate associated user accounts
        userRepo.findAll().stream()
                .filter(u -> u.getEmployee() != null && u.getEmployee().getId().equals(id))
                .forEach(u -> { u.setIsActive(false); userRepo.save(u); });
        employeeRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Employee deleted"));
    }

    // ─── USERS ────────────────────────────────────────────────────

    @GetMapping("/users")
    public ResponseEntity<?> listUsers(HttpSession session) {
        if (!isAdmin(session)) return forbidden();
        List<Map<String, Object>> result = new ArrayList<>();
        for (User u : userRepo.findAll()) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("role", u.getRole().name());
            m.put("isActive", u.getIsActive());
            if (u.getEmployee() != null) {
                m.put("employeeId", u.getEmployee().getId());
                m.put("employeeName", u.getEmployee().getFirstName() + " " + u.getEmployee().getLastName());
                m.put("employeeCode", u.getEmployee().getEmployeeId());
            }
            result.add(m);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody Map<String, Object> body, HttpSession session) {
        if (!isAdmin(session)) return forbidden();
        User u = new User();
        u.setUsername((String) body.get("username"));
        u.setPassword((String) body.get("password"));
        u.setRole(User.UserRole.valueOf((String) body.getOrDefault("role", "EMPLOYEE")));
        u.setIsActive(true);
        Object empId = body.get("employeeId");
        if (empId != null) {
            employeeRepo.findById(Long.parseLong(empId.toString())).ifPresent(u::setEmployee);
        }
        return ResponseEntity.ok(userRepo.save(u));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpSession session) {
        if (!isAdmin(session)) return forbidden();
        return userRepo.findById(id).map(u -> {
            if (body.containsKey("password") && !((String) body.get("password")).isBlank()) {
                u.setPassword((String) body.get("password"));
            }
            if (body.containsKey("role")) u.setRole(User.UserRole.valueOf((String) body.get("role")));
            if (body.containsKey("isActive")) u.setIsActive((Boolean) body.get("isActive"));
            return ResponseEntity.ok(userRepo.save(u));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/users/{id}/toggle")
    public ResponseEntity<?> toggleUser(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) return forbidden();
        return userRepo.findById(id).map(u -> {
            u.setIsActive(!Boolean.TRUE.equals(u.getIsActive()));
            userRepo.save(u);
            return ResponseEntity.ok(Map.of("isActive", u.getIsActive()));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ─── ATTENDANCE ───────────────────────────────────────────────

    @GetMapping("/attendance/{employeeId}")
    public ResponseEntity<?> getAttendance(@PathVariable Long employeeId,
                                           @RequestParam(defaultValue = "30") int days,
                                           HttpSession session) {
        if (!isAdmin(session)) return forbidden();
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(days);
        return ResponseEntity.ok(
                attendanceRepo.findByEmployee_IdAndCheckInTimeBetweenOrderByCheckInTimeDesc(employeeId, start, end));
    }

    // ─── MONTHLY SUMMARY ──────────────────────────────────────────

    @GetMapping("/monthly-summary/{employeeId}")
    public ResponseEntity<?> getMonthlySummary(@PathVariable Long employeeId,
                                               @RequestParam int month,
                                               @RequestParam int year,
                                               HttpSession session) {
        if (!isAdmin(session)) return forbidden();
        try {
            return ResponseEntity.ok(monthlySummaryService.compute(employeeId, month, year));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── BONUS / PENALTY ──────────────────────────────────────────

    @GetMapping("/bonus-penalty")
    public ResponseEntity<?> listBonusPenalty(HttpSession session) {
        if (!isAdmin(session)) return forbidden();
        return ResponseEntity.ok(bonusPenaltyService.getAll());
    }

    @PostMapping("/bonus-penalty")
    public ResponseEntity<?> createBonusPenalty(@RequestBody Map<String, Object> body, HttpSession session) {
        if (!isAdmin(session)) return forbidden();
        User admin = currentUser(session);
        Long empId = Long.parseLong(body.get("employeeId").toString());
        return employeeRepo.findById(empId).map(emp -> {
            BonusPenalty bp = bonusPenaltyService.create(
                    emp,
                    BonusPenalty.RecordType.valueOf((String) body.get("type")),
                    new BigDecimal(body.get("amount").toString()),
                    (String) body.getOrDefault("reason", ""),
                    Integer.parseInt(body.get("month").toString()),
                    Integer.parseInt(body.get("year").toString()),
                    admin);
            // Notify employee
            userRepo.findAll().stream()
                    .filter(u -> u.getEmployee() != null && u.getEmployee().getId().equals(empId))
                    .findFirst()
                    .ifPresent(u -> notificationService.create(u,
                            bp.getType() + ": " + bp.getAmount() + " - " + bp.getReason(),
                            bp.getType().name(), bp.getId()));
            return ResponseEntity.ok(bp);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/bonus-penalty/{id}")
    public ResponseEntity<?> deleteBonusPenalty(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) return forbidden();
        bonusPenaltyService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }

    // ─── ANNOUNCEMENTS ────────────────────────────────────────────

    @GetMapping("/announcements")
    public ResponseEntity<?> listAnnouncements(HttpSession session) {
        if (!isAdmin(session)) return forbidden();
        return ResponseEntity.ok(announcementService.getAll());
    }

    @PostMapping("/announcements")
    public ResponseEntity<?> createAnnouncement(@RequestBody Map<String, Object> body, HttpSession session) {
        if (!isAdmin(session)) return forbidden();
        User admin = currentUser(session);
        Announcement a = announcementService.create(
                admin,
                Announcement.TargetType.valueOf((String) body.getOrDefault("targetType", "ALL")),
                (String) body.get("targetValue"),
                (String) body.get("title"),
                (String) body.get("content"));
        return ResponseEntity.ok(a);
    }

    // ─── LEAVE REQUESTS (admin overview) ─────────────────────────

    @GetMapping("/leave-requests")
    public ResponseEntity<?> listLeaveRequests(HttpSession session) {
        if (!isAdmin(session)) return forbidden();
        return ResponseEntity.ok(leaveRepo.findAll());
    }

    @PutMapping("/leave-requests/{id}/approve")
    public ResponseEntity<?> approveLeave(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) return forbidden();
        return leaveRepo.findById(id).map(lr -> {
            lr.setStatus(LeaveRequest.LeaveStatus.APPROVED);
            lr.setApprovalDate(LocalDateTime.now());
            leaveRepo.save(lr);
            userRepo.findAll().stream()
                    .filter(u -> u.getEmployee() != null && u.getEmployee().getId().equals(lr.getEmployee().getId()))
                    .findFirst()
                    .ifPresent(u -> notificationService.create(u, "Your leave request has been approved.", "LEAVE_APPROVED", lr.getId()));
            return ResponseEntity.ok(lr);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/leave-requests/{id}/reject")
    public ResponseEntity<?> rejectLeave(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body, HttpSession session) {
        if (!isAdmin(session)) return forbidden();
        return leaveRepo.findById(id).map(lr -> {
            lr.setStatus(LeaveRequest.LeaveStatus.REJECTED);
            lr.setApprovalDate(LocalDateTime.now());
            if (body != null && body.containsKey("comment")) lr.setApproverComments(body.get("comment"));
            leaveRepo.save(lr);
            userRepo.findAll().stream()
                    .filter(u -> u.getEmployee() != null && u.getEmployee().getId().equals(lr.getEmployee().getId()))
                    .findFirst()
                    .ifPresent(u -> notificationService.create(u, "Your leave request has been rejected.", "LEAVE_REJECTED", lr.getId()));
            return ResponseEntity.ok(lr);
        }).orElse(ResponseEntity.notFound().build());
    }

    // ─── DASHBOARD STATS ──────────────────────────────────────────

    @GetMapping("/stats")
    public ResponseEntity<?> stats(HttpSession session) {
        if (!isAdmin(session)) return forbidden();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(23, 59, 59);
        long totalEmployees = employeeRepo.count();
        long presentToday = attendanceRepo.findAll().stream()
                .filter(r -> r.getCheckInTime() != null
                        && !r.getCheckInTime().isBefore(todayStart)
                        && !r.getCheckInTime().isAfter(todayEnd))
                .map(r -> r.getEmployee().getId()).distinct().count();
        long pendingLeaves = leaveRepo.findByStatus(LeaveRequest.LeaveStatus.PENDING).size();
        long totalUsers = userRepo.count();
        return ResponseEntity.ok(Map.of(
                "totalEmployees", totalEmployees,
                "presentToday", presentToday,
                "pendingLeaves", pendingLeaves,
                "totalUsers", totalUsers));
    }
}
