/*
 * FILE: EmployeeApiController.java
 * PURPOSE: Session-authenticated REST controller for logged-in employees at /api/employee.
 *          All endpoints resolve the caller from the HttpSession (userId → User → Employee).
 *          Covers the full employee-facing API: personal dashboard, check-in/out,
 *          attendance history, monthly summary, bonus/penalty, leave requests,
 *          re-check-in requests, and notifications.
 *
 * HELPER METHODS:
 *  - currentUser(session)    Resolves User by session userId.
 *  - currentEmployee(session) Returns the linked Employee of the current user.
 *  - noEmployee()            Returns 400 "No employee record linked" response.
 *
 * ENDPOINTS:
 *  - GET /api/employee/me
 *      Returns the Employee entity linked to the current session.
 *
 *  - GET /api/employee/dashboard
 *      Returns a rich map for the Personal Dashboard: name, dept, position, baseSalary,
 *      role, today's check-in state (NOT_CHECKED_IN/CHECKED_IN/CHECKED_OUT),
 *      check-in/out timestamps, monthly bonus, monthly penalty, working days this month.
 *
 *  - POST /api/employee/checkin
 *      Creates an attendance record with status PRESENT (or LATE if after 09:00).
 *      Guards against duplicate same-day check-ins.
 *
 *  - POST /api/employee/checkout
 *      Finds the latest open attendance record (no checkOutTime) and closes it.
 *      Auto-sets status to HALF_DAY if hours worked < 4.
 *
 *  - GET /api/employee/today-status
 *      Returns the current check-in state and timestamps for the current day.
 *      Polled by the frontend to keep the UI in sync.
 *
 *  - GET /api/employee/attendance?days=30
 *      Returns attendance records from the last N days (default 30).
 *
 *  - GET /api/employee/monthly-summary?month=&year=
 *      Delegates to MonthlySummaryService.compute() for the salary/attendance report.
 *
 *  - GET /api/employee/bonus-penalty
 *      Returns all bonus/penalty records for the current employee.
 *
 *  - GET /api/employee/leave-requests
 *      Returns all leave requests submitted by the current employee.
 *
 *  - POST /api/employee/leave-requests  { leaveType, startDate, endDate, reason }
 *      Submits a new leave request with PENDING status.
 *
 *  - DELETE /api/employee/leave-requests/{id}
 *      Cancels a PENDING leave request. Returns 403 if not owned by caller,
 *      400 if already processed.
 *
 *  - GET /api/employee/recheckin-requests
 *      Lists the current employee's re-check-in request history.
 *
 *  - POST /api/employee/recheckin-requests  { requestedDate, requestedCheckinTime, reason }
 *      Submits a new re-check-in correction request.
 *
 *  - GET /api/employee/notifications
 *      Returns all notifications for the current user.
 *
 *  - PUT /api/employee/notifications/{id}/read
 *      Marks a single notification as read.
 *
 *  - PUT /api/employee/notifications/read-all
 *      Marks all notifications as read for the current user.
 *
 * HOW TO MODIFY:
 *  - To change the LATE threshold (currently 09:00): update the hour comparison
 *    in the checkIn() method.
 *  - To add new dashboard fields: extend the getDashboard() method and return
 *    additional data in the map.
 *  - To add task-related endpoints here: see TaskApiController, which handles
 *    GET/POST /api/employee/tasks separately.
 */
package com.example.demo.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

import com.example.demo.entity.AttendanceRecord;
import com.example.demo.entity.Employee;
import com.example.demo.entity.LeaveRequest;
import com.example.demo.entity.User;
import com.example.demo.repository.AttendanceRecordRepository;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.LeaveRequestRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.BonusPenaltyService;
import com.example.demo.service.MonthlySummaryService;
import com.example.demo.service.NotificationService;
import com.example.demo.service.ReCheckinRequestService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/employee")
public class EmployeeApiController {

    @Autowired private EmployeeRepository employeeRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private AttendanceRecordRepository attendanceRepo;
    @Autowired private LeaveRequestRepository leaveRepo;
    @Autowired private ReCheckinRequestService reCheckinService;
    @Autowired private BonusPenaltyService bonusPenaltyService;
    @Autowired private MonthlySummaryService monthlySummaryService;
    @Autowired private NotificationService notificationService;

    private User currentUser(HttpSession s) {
        Long id = (Long) s.getAttribute("userId");
        return id == null ? null : userRepo.findById(id).orElse(null);
    }

    private Employee currentEmployee(HttpSession s) {
        User u = currentUser(s);
        return (u != null) ? u.getEmployee() : null;
    }

    private ResponseEntity<?> noEmployee() {
        return ResponseEntity.status(400).body(Map.of("error", "No employee record linked to this account"));
    }

    // ─── PROFILE ──────────────────────────────────────────────────

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        Employee emp = currentEmployee(session);
        if (emp == null) return noEmployee();
        return ResponseEntity.ok(emp);
    }

    // ─── PERSONAL DASHBOARD ───────────────────────────────────────

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(HttpSession session) {
        Employee emp = currentEmployee(session);
        if (emp == null) return noEmployee();
        User user = currentUser(session);

        // Today's check-in status
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(23, 59, 59);
        List<AttendanceRecord> todayRecords = attendanceRepo
                .findByEmployee_IdAndCheckInTimeBetweenOrderByCheckInTimeDesc(emp.getId(), todayStart, todayEnd);

        String todayState = "NOT_CHECKED_IN";
        LocalDateTime checkInTime = null;
        LocalDateTime checkOutTime = null;
        if (!todayRecords.isEmpty()) {
            AttendanceRecord latest = todayRecords.get(0);
            checkInTime = latest.getCheckInTime();
            checkOutTime = latest.getCheckOutTime();
            todayState = checkOutTime == null ? "CHECKED_IN" : "CHECKED_OUT";
        }

        // This month bonus / penalty
        LocalDate now = LocalDate.now();
        BigDecimal monthlyBonus = bonusPenaltyService.sumBonus(emp.getId(), now.getMonthValue(), now.getYear());
        BigDecimal monthlyPenalty = bonusPenaltyService.sumPenalty(emp.getId(), now.getMonthValue(), now.getYear());
        if (monthlyBonus == null) monthlyBonus = BigDecimal.ZERO;
        if (monthlyPenalty == null) monthlyPenalty = BigDecimal.ZERO;

        // Working days this month (up to today)
        LocalDateTime monthStart = now.withDayOfMonth(1).atStartOfDay();
        List<AttendanceRecord> monthRecords = attendanceRepo
                .findByEmployee_IdAndCheckInTimeBetweenOrderByCheckInTimeDesc(emp.getId(), monthStart, todayEnd);
        long workingDays = monthRecords.stream()
                .filter(r -> r.getStatus() != null &&
                        (r.getStatus() == AttendanceRecord.AttendanceStatus.PRESENT ||
                         r.getStatus() == AttendanceRecord.AttendanceStatus.LATE ||
                         r.getStatus() == AttendanceRecord.AttendanceStatus.HALF_DAY))
                .count();

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("firstName", emp.getFirstName());
        dashboard.put("lastName", emp.getLastName());
        dashboard.put("department", emp.getDepartment());
        dashboard.put("position", emp.getPosition());
        dashboard.put("baseSalary", emp.getBaseSalary() != null ? emp.getBaseSalary() : BigDecimal.ZERO);
        dashboard.put("role", user != null ? user.getRole().name() : "EMPLOYEE");
        dashboard.put("todayState", todayState);
        dashboard.put("checkInTime", checkInTime);
        dashboard.put("checkOutTime", checkOutTime);
        dashboard.put("monthlyBonus", monthlyBonus);
        dashboard.put("monthlyPenalty", monthlyPenalty);
        dashboard.put("workingDaysThisMonth", workingDays);
        return ResponseEntity.ok(dashboard);
    }

    // ─── CHECK-IN / CHECK-OUT ─────────────────────────────────────

    @PostMapping("/checkin")
    public ResponseEntity<?> checkIn(HttpSession session) {
        Employee emp = currentEmployee(session);
        if (emp == null) return noEmployee();

        // Check for duplicate check-in today
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(23, 59, 59);
        List<AttendanceRecord> todayRecords = attendanceRepo
                .findByEmployee_IdAndCheckInTimeBetweenOrderByCheckInTimeDesc(emp.getId(), todayStart, todayEnd);
        if (!todayRecords.isEmpty() && todayRecords.get(0).getCheckOutTime() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Already checked in. Please check out first."));
        }

        // Auto-checkout logic: Nếu đã check in trước đó 1 tiếng và bây giờ sau 18h
        attendanceRepo.findTopByEmployee_IdAndCheckOutTimeIsNullOrderByCheckInTimeDesc(emp.getId())
            .ifPresent(record -> {
                LocalDateTime checkInTime = record.getCheckInTime();
                LocalDateTime autoCheckoutTime = now.withHour(18).withMinute(0).withSecond(0).withNano(0);
                if (checkInTime != null && now.isAfter(autoCheckoutTime)
                    && java.time.Duration.between(checkInTime, autoCheckoutTime).toHours() >= 1
                    && record.getCheckOutTime() == null) {
                    record.setCheckOutTime(autoCheckoutTime);
                    record.calculateHoursWorked();
                    if (record.getHoursWorked() != null && record.getHoursWorked() < 4.0) {
                        record.setStatus(AttendanceRecord.AttendanceStatus.HALF_DAY);
                    }
                    attendanceRepo.save(record);
                }
            });

        AttendanceRecord record = new AttendanceRecord();
        record.setEmployee(emp);
        record.setCheckInTime(now);
        // Mark as LATE if after 9:00 AM
        if (now.getHour() >= 9) {
            record.setStatus(AttendanceRecord.AttendanceStatus.LATE);
        } else {
            record.setStatus(AttendanceRecord.AttendanceStatus.PRESENT);
        }
        return ResponseEntity.ok(attendanceRepo.save(record));
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkOut(HttpSession session) {
        Employee emp = currentEmployee(session);
        if (emp == null) return noEmployee();

        return attendanceRepo
                .findTopByEmployee_IdAndCheckOutTimeIsNullOrderByCheckInTimeDesc(emp.getId())
                .<ResponseEntity<?>>map(record -> {
                    record.setCheckOutTime(LocalDateTime.now());
                    record.calculateHoursWorked();
                    // Mark as HALF_DAY if worked less than 4 hours
                    if (record.getHoursWorked() != null && record.getHoursWorked() < 4.0) {
                        record.setStatus(AttendanceRecord.AttendanceStatus.HALF_DAY);
                    }
                    return ResponseEntity.ok(attendanceRepo.save(record));
                })
                .orElse(ResponseEntity.badRequest().body(Map.of("error", "No active check-in found.")));
    }

    @GetMapping("/today-status")
    public ResponseEntity<?> todayStatus(HttpSession session) {
        Employee emp = currentEmployee(session);
        if (emp == null) return noEmployee();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(23, 59, 59);
        List<AttendanceRecord> todayRecords = attendanceRepo
                .findByEmployee_IdAndCheckInTimeBetweenOrderByCheckInTimeDesc(emp.getId(), todayStart, todayEnd);
        Map<String, Object> status = new HashMap<>();
        if (todayRecords.isEmpty()) {
            status.put("state", "NOT_CHECKED_IN");
        } else {
            AttendanceRecord latest = todayRecords.get(0);
            status.put("state", latest.getCheckOutTime() == null ? "CHECKED_IN" : "CHECKED_OUT");
            status.put("checkInTime", latest.getCheckInTime());
            status.put("checkOutTime", latest.getCheckOutTime());
            status.put("recordId", latest.getId());
        }
        return ResponseEntity.ok(status);
    }

    // ─── ATTENDANCE HISTORY ───────────────────────────────────────

    @GetMapping("/attendance")
    public ResponseEntity<?> getAttendance(@RequestParam(defaultValue = "30") int days,
                                           HttpSession session) {
        Employee emp = currentEmployee(session);
        if (emp == null) return noEmployee();
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(days);
        return ResponseEntity.ok(
                attendanceRepo.findByEmployee_IdAndCheckInTimeBetweenOrderByCheckInTimeDesc(emp.getId(), start, end));
    }

    // ─── MONTHLY SUMMARY ──────────────────────────────────────────

    @GetMapping("/monthly-summary")
    public ResponseEntity<?> getMonthlySummary(@RequestParam int month,
                                               @RequestParam int year,
                                               HttpSession session) {
        Employee emp = currentEmployee(session);
        if (emp == null) return noEmployee();
        try {
            return ResponseEntity.ok(monthlySummaryService.compute(emp.getId(), month, year));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── BONUS / PENALTY ──────────────────────────────────────────

    @GetMapping("/bonus-penalty")
    public ResponseEntity<?> getBonusPenalty(HttpSession session) {
        Employee emp = currentEmployee(session);
        if (emp == null) return noEmployee();
        return ResponseEntity.ok(bonusPenaltyService.getByEmployee(emp.getId()));
    }

    // ─── LEAVE REQUESTS ───────────────────────────────────────────

    @GetMapping("/leave-requests")
    public ResponseEntity<?> getLeaveRequests(HttpSession session) {
        Employee emp = currentEmployee(session);
        if (emp == null) return noEmployee();
        return ResponseEntity.ok(leaveRepo.findByEmployee_IdOrderByRequestDateDesc(emp.getId()));
    }

    @PostMapping("/leave-requests")
    public ResponseEntity<?> submitLeave(@RequestBody Map<String, Object> body, HttpSession session) {
        Employee emp = currentEmployee(session);
        if (emp == null) return noEmployee();
        LeaveRequest lr = new LeaveRequest();
        lr.setEmployee(emp);
        lr.setLeaveType(LeaveRequest.LeaveType.valueOf((String) body.getOrDefault("leaveType", "PERSONAL")));
        lr.setStartDate(LocalDate.parse((String) body.get("startDate")));
        lr.setEndDate(LocalDate.parse((String) body.get("endDate")));
        lr.setReason((String) body.getOrDefault("reason", ""));
        lr.setStatus(LeaveRequest.LeaveStatus.PENDING);
        lr.setRequestDate(LocalDateTime.now());
        return ResponseEntity.ok(leaveRepo.save(lr));
    }

    @DeleteMapping("/leave-requests/{id}")
    public ResponseEntity<?> cancelLeave(@PathVariable Long id, HttpSession session) {
        Employee emp = currentEmployee(session);
        if (emp == null) return noEmployee();
        return leaveRepo.findById(id).map(lr -> {
            if (!lr.getEmployee().getId().equals(emp.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Not your leave request"));
            }
            if (lr.getStatus() != LeaveRequest.LeaveStatus.PENDING) {
                return ResponseEntity.badRequest().body(Map.of("error", "Only pending requests can be cancelled"));
            }
            lr.setStatus(LeaveRequest.LeaveStatus.CANCELLED);
            leaveRepo.save(lr);
            return ResponseEntity.ok(Map.of("message", "Cancelled"));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ─── RE-CHECKIN REQUESTS ──────────────────────────────────────

    @GetMapping("/recheckin-requests")
    public ResponseEntity<?> getReCheckinRequests(HttpSession session) {
        Employee emp = currentEmployee(session);
        if (emp == null) return noEmployee();
        return ResponseEntity.ok(reCheckinService.getByEmployee(emp.getId()));
    }

    @PostMapping("/recheckin-requests")
    public ResponseEntity<?> submitReCheckin(@RequestBody Map<String, Object> body, HttpSession session) {
        Employee emp = currentEmployee(session);
        if (emp == null) return noEmployee();
        LocalDate requestedDate = LocalDate.parse((String) body.get("requestedDate"));
        LocalDateTime requestedTime = LocalDateTime.parse((String) body.get("requestedCheckinTime"));
        String reason = (String) body.getOrDefault("reason", "");
        return ResponseEntity.ok(reCheckinService.submit(emp, requestedDate, requestedTime, reason));
    }

    // ─── NOTIFICATIONS ────────────────────────────────────────────

    @GetMapping("/notifications")
    public ResponseEntity<?> getNotifications(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(notificationService.getAll(userId));
    }

    @PutMapping("/notifications/{id}/read")
    public ResponseEntity<?> markRead(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).build();
        notificationService.markRead(id);
        return ResponseEntity.ok(Map.of("message", "Marked read"));
    }

    @PutMapping("/notifications/read-all")
    public ResponseEntity<?> markAllRead(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).build();
        notificationService.markAllRead(userId);
        return ResponseEntity.ok(Map.of("message", "All marked read"));
    }
}
