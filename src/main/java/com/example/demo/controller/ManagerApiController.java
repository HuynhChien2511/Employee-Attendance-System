/*
 * FILE: ManagerApiController.java
 * PURPOSE: Session-authenticated REST controller for manager operations at /api/manager.
 *          Every endpoint checks isManager(session) and returns 403 if caller is not MANAGER.
 *          Managers can only access their direct team (employees where managerUserId matches
 *          the session's userId). Cross-team access is blocked with a 403.
 *
 * HELPER METHODS:
 *  - isManager(session)   Returns true if session.userRole == "MANAGER".
 *  - forbidden()          Returns 403 response.
 *  - currentUser(session) Resolves User by session userId.
 *
 * ENDPOINTS — TEAM (/api/manager/team):
 *  - GET  Returns all employees whose managerUserId matches the current manager.
 *
 * ENDPOINTS — ATTENDANCE (/api/manager/attendance/{employeeId}):
 *  - GET ?days=30  Returns attendance records for a team member in the last N days.
 *    Returns 403 if the employee is not in this manager's team.
 *
 * ENDPOINTS — MONTHLY SUMMARY (/api/manager/monthly-summary/{employeeId}):
 *  - GET ?month=&year=  Runs MonthlySummaryService.compute() for a team member.
 *    Returns 403 if the employee does not belong to this manager.
 *
 * ENDPOINTS — LEAVE REQUESTS (/api/manager/leave-requests):
 *  - GET   Returns all leave requests from the manager's team, sorted by date.
 *  - PUT /{id}/approve  Approves and sends a notification to the employee.
 *  - PUT /{id}/reject   Rejects (optionally with comment) and notifies employee.
 *    Both validate team membership before acting.
 *
 * ENDPOINTS — RE-CHECKIN REQUESTS (/api/manager/recheckin-requests):
 *  - GET   Returns all re-check-in requests from the team.
 *  - PUT /{id}/approve  Approves via ReCheckinRequestService (creates attendance record)
 *    and notifies employee.
 *  - PUT /{id}/reject   Rejects and notifies employee.
 *
 * ENDPOINTS — ANNOUNCEMENTS (/api/manager/announcements):
 *  - POST  Creates an announcement. Body: targetType, targetValue, title, content.
 *
 * ENDPOINTS — BONUS/PENALTY (/api/manager/bonus-penalty):
 *  - GET   Returns all bonus/penalty records for the manager's team combined, sorted newest-first.
 *  - POST  Creates a bonus/penalty for a team member (validates team membership) and notifies.
 *
 * ENDPOINTS — STATS (/api/manager/stats):
 *  - GET  Returns: teamSize, pendingLeaves, pendingReCheckins.
 *
 * HOW TO MODIFY:
 *  - To allow managers to also see ADMIN-level data: add an OR check for "ADMIN"
 *    in isManager() or create a separate helper like isManagerOrAdmin().
 *  - To add task assignment for managers: see TaskApiController which already
 *    allows MANAGER role to POST /api/tasks and manage tasks.
 *  - To add bulk approval: add a POST /api/manager/leave-requests/approve-all endpoint
 *    that iterates over pending requests for the team.
 */
package com.example.demo.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
import com.example.demo.service.ReCheckinRequestService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/manager")
public class ManagerApiController {

    @Autowired private EmployeeRepository employeeRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private AttendanceRecordRepository attendanceRepo;
    @Autowired private LeaveRequestRepository leaveRepo;
    @Autowired private ReCheckinRequestService reCheckinService;
    @Autowired private BonusPenaltyService bonusPenaltyService;
    @Autowired private AnnouncementService announcementService;
    @Autowired private NotificationService notificationService;
    @Autowired private MonthlySummaryService monthlySummaryService;

    private boolean isManager(HttpSession s) {
        return "MANAGER".equals(s.getAttribute("userRole"));
    }

    private ResponseEntity<?> forbidden() {
        return ResponseEntity.status(403).body(Map.of("error", "Manager access required"));
    }

    private User currentUser(HttpSession s) {
        Long id = (Long) s.getAttribute("userId");
        return id == null ? null : userRepo.findById(id).orElse(null);
    }

    private ResponseEntity<?> noEmployee() {
        return ResponseEntity.status(400).body(Map.of("error", "No employee record linked to this manager account"));
    }

    // ─── TEAM ─────────────────────────────────────────────────────

    @GetMapping("/team")
    public ResponseEntity<?> getTeam(HttpSession session) {
        if (!isManager(session)) return forbidden();
        Long userId = (Long) session.getAttribute("userId");
        return ResponseEntity.ok(employeeRepo.findByManagerUserId(userId));
    }

    // ─── PROFILE ──────────────────────────────────────────────────

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpSession session) {
        if (!isManager(session)) return forbidden();
        User user = currentUser(session);
        if (user == null || user.getEmployee() == null) return noEmployee();
        Employee emp = user.getEmployee();

        Map<String, Object> profile = new HashMap<>();
        profile.put("firstName", emp.getFirstName());
        profile.put("lastName", emp.getLastName());
        profile.put("role", user.getRole().toString());
        profile.put("department", emp.getDepartment());
        profile.put("position", emp.getPosition());
        profile.put("baseSalary", emp.getBaseSalary());

        // Today's status
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(23, 59, 59);
        List<com.example.demo.entity.AttendanceRecord> todayRecords = attendanceRepo
                .findByEmployee_IdAndCheckInTimeBetweenOrderByCheckInTimeDesc(emp.getId(), todayStart, todayEnd);
        
        String todayStatus = "NOT_CHECKED_IN";
        LocalDateTime checkInTime = null;
        LocalDateTime checkOutTime = null;
        if (!todayRecords.isEmpty()) {
            com.example.demo.entity.AttendanceRecord latest = todayRecords.get(0);
            todayStatus = latest.getCheckOutTime() == null ? "CHECKED_IN" : "CHECKED_OUT";
            checkInTime = latest.getCheckInTime();
            checkOutTime = latest.getCheckOutTime();
        }
        profile.put("todayStatus", todayStatus);
        profile.put("checkInTime", checkInTime);
        profile.put("checkOutTime", checkOutTime);

        // Monthly bonus/penalty (current month)
        LocalDate now = LocalDate.now();
        BigDecimal monthlyBonus = bonusPenaltyService.sumBonus(emp.getId(), now.getMonthValue(), now.getYear());
        BigDecimal monthlyPenalty = bonusPenaltyService.sumPenalty(emp.getId(), now.getMonthValue(), now.getYear());
        profile.put("monthlyBonus", monthlyBonus);
        profile.put("monthlyPenalty", monthlyPenalty);

        // Salary coefficient (relative to base salary, default 1.0)
        profile.put("salaryCoefficient", 1.0);

        // Working days this month
        try {
            Map<String, Object> monthlySummary = monthlySummaryService.compute(emp.getId(), now.getMonthValue(), now.getYear());
            Integer presentDays = (Integer) monthlySummary.getOrDefault("presentDays", 0);
            profile.put("workingDaysThisMonth", presentDays);
        } catch (Exception e) {
            profile.put("workingDaysThisMonth", 0);
        }

        return ResponseEntity.ok(profile);
    }

    // ─── ATTENDANCE ───────────────────────────────────────────────

    @PostMapping("/checkin")
    public ResponseEntity<?> checkIn(HttpSession session) {
        if (!isManager(session)) return forbidden();
        User user = currentUser(session);
        if (user == null || user.getEmployee() == null) return noEmployee();
        Employee emp = user.getEmployee();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(23, 59, 59);
        List<com.example.demo.entity.AttendanceRecord> todayRecords = attendanceRepo
                .findByEmployee_IdAndCheckInTimeBetweenOrderByCheckInTimeDesc(emp.getId(), todayStart, todayEnd);
        if (!todayRecords.isEmpty() && todayRecords.get(0).getCheckOutTime() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Already checked in. Please check out first."));
        }

        com.example.demo.entity.AttendanceRecord record = new com.example.demo.entity.AttendanceRecord();
        record.setEmployee(emp);
        record.setCheckInTime(now);
        if (now.getHour() >= 9) {
            record.setStatus(com.example.demo.entity.AttendanceRecord.AttendanceStatus.LATE);
        } else {
            record.setStatus(com.example.demo.entity.AttendanceRecord.AttendanceStatus.PRESENT);
        }
        return ResponseEntity.ok(attendanceRepo.save(record));
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkOut(HttpSession session) {
        if (!isManager(session)) return forbidden();
        User user = currentUser(session);
        if (user == null || user.getEmployee() == null) return noEmployee();
        Employee emp = user.getEmployee();

        return attendanceRepo
                .findTopByEmployee_IdAndCheckOutTimeIsNullOrderByCheckInTimeDesc(emp.getId())
                .<ResponseEntity<?>>map(record -> {
                    record.setCheckOutTime(LocalDateTime.now());
                    record.calculateHoursWorked();
                    if (record.getHoursWorked() != null && record.getHoursWorked() < 4.0) {
                        record.setStatus(com.example.demo.entity.AttendanceRecord.AttendanceStatus.HALF_DAY);
                    }
                    return ResponseEntity.ok(attendanceRepo.save(record));
                })
                .orElse(ResponseEntity.badRequest().body(Map.of("error", "No active check-in found.")));
    }

    @GetMapping("/today-status")
    public ResponseEntity<?> todayStatus(HttpSession session) {
        if (!isManager(session)) return forbidden();
        User user = currentUser(session);
        if (user == null || user.getEmployee() == null) return noEmployee();
        Employee emp = user.getEmployee();

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(23, 59, 59);
        List<com.example.demo.entity.AttendanceRecord> todayRecords = attendanceRepo
                .findByEmployee_IdAndCheckInTimeBetweenOrderByCheckInTimeDesc(emp.getId(), todayStart, todayEnd);

        Map<String, Object> status = new HashMap<>();
        if (todayRecords.isEmpty()) {
            status.put("state", "NOT_CHECKED_IN");
        } else {
            com.example.demo.entity.AttendanceRecord latest = todayRecords.get(0);
            status.put("state", latest.getCheckOutTime() == null ? "CHECKED_IN" : "CHECKED_OUT");
            status.put("checkInTime", latest.getCheckInTime());
            status.put("checkOutTime", latest.getCheckOutTime());
            status.put("recordId", latest.getId());
        }
        return ResponseEntity.ok(status);
    }

    @GetMapping("/attendance/{employeeId}")
    public ResponseEntity<?> getAttendance(@PathVariable Long employeeId,
                                           @RequestParam(defaultValue = "30") int days,
                                           HttpSession session) {
        if (!isManager(session)) return forbidden();
        Long userId = (Long) session.getAttribute("userId");
        return employeeRepo.findById(employeeId).map(emp -> {
            if (!userId.equals(emp.getManagerUserId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Not your team member"));
            }
            LocalDateTime end = LocalDateTime.now();
            LocalDateTime start = end.minusDays(days);
            return ResponseEntity.ok(
                    attendanceRepo.findByEmployee_IdAndCheckInTimeBetweenOrderByCheckInTimeDesc(employeeId, start, end));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ─── MONTHLY SUMMARY ──────────────────────────────────────────

    @GetMapping("/monthly-summary/{employeeId}")
    public ResponseEntity<?> getMonthlySummary(@PathVariable Long employeeId,
                                               @RequestParam int month,
                                               @RequestParam int year,
                                               HttpSession session) {
        if (!isManager(session)) return forbidden();
        Long userId = (Long) session.getAttribute("userId");
        return employeeRepo.findById(employeeId).map(emp -> {
            if (!userId.equals(emp.getManagerUserId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Not your team member"));
            }
            try {
                return ResponseEntity.ok(monthlySummaryService.compute(employeeId, month, year));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    // ─── LEAVE REQUESTS ───────────────────────────────────────────

    @GetMapping("/leave-requests")
    public ResponseEntity<?> getLeaveRequests(HttpSession session) {
        if (!isManager(session)) return forbidden();
        Long userId = (Long) session.getAttribute("userId");
        return ResponseEntity.ok(leaveRepo.findByEmployee_ManagerUserIdOrderByRequestDateDesc(userId));
    }

    @PutMapping("/leave-requests/{id}/approve")
    public ResponseEntity<?> approveLeave(@PathVariable Long id, HttpSession session) {
        if (!isManager(session)) return forbidden();
        Long userId = (Long) session.getAttribute("userId");
        return leaveRepo.findById(id).map(lr -> {
            if (!userId.equals(lr.getEmployee().getManagerUserId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Not your team member"));
            }
            lr.setStatus(LeaveRequest.LeaveStatus.APPROVED);
            lr.setApprovalDate(LocalDateTime.now());
            leaveRepo.save(lr);
            userRepo.findAll().stream()
                    .filter(u -> u.getEmployee() != null && u.getEmployee().getId().equals(lr.getEmployee().getId()))
                    .findFirst()
                    .ifPresent(u -> notificationService.create(u,
                            "Your leave request has been approved.", "LEAVE_APPROVED", lr.getId()));
            return ResponseEntity.ok(lr);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/leave-requests/{id}/reject")
    public ResponseEntity<?> rejectLeave(@PathVariable Long id,
                                         @RequestBody(required = false) Map<String, String> body,
                                         HttpSession session) {
        if (!isManager(session)) return forbidden();
        Long userId = (Long) session.getAttribute("userId");
        return leaveRepo.findById(id).map(lr -> {
            if (!userId.equals(lr.getEmployee().getManagerUserId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Not your team member"));
            }
            lr.setStatus(LeaveRequest.LeaveStatus.REJECTED);
            lr.setApprovalDate(LocalDateTime.now());
            if (body != null && body.containsKey("comment")) lr.setApproverComments(body.get("comment"));
            leaveRepo.save(lr);
            userRepo.findAll().stream()
                    .filter(u -> u.getEmployee() != null && u.getEmployee().getId().equals(lr.getEmployee().getId()))
                    .findFirst()
                    .ifPresent(u -> notificationService.create(u,
                            "Your leave request has been rejected.", "LEAVE_REJECTED", lr.getId()));
            return ResponseEntity.ok(lr);
        }).orElse(ResponseEntity.notFound().build());
    }

    // ─── RE-CHECKIN REQUESTS ──────────────────────────────────────

    @GetMapping("/recheckin-requests")
    public ResponseEntity<?> getReCheckinRequests(HttpSession session) {
        if (!isManager(session)) return forbidden();
        Long userId = (Long) session.getAttribute("userId");
        return ResponseEntity.ok(reCheckinService.getAllForManager(userId));
    }

    @PutMapping("/recheckin-requests/{id}/approve")
    public ResponseEntity<?> approveReCheckin(@PathVariable Long id, HttpSession session) {
        if (!isManager(session)) return forbidden();
        User manager = currentUser(session);
        return reCheckinService.approve(id, manager)
                .<ResponseEntity<?>>map(r -> {
                    userRepo.findAll().stream()
                            .filter(u -> u.getEmployee() != null && u.getEmployee().getId().equals(r.getEmployee().getId()))
                            .findFirst()
                            .ifPresent(u -> notificationService.create(u,
                                    "Your re-check-in request has been approved.", "RECHECKIN_APPROVED", r.getId()));
                    return ResponseEntity.ok(r);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/recheckin-requests/{id}/reject")
    public ResponseEntity<?> rejectReCheckin(@PathVariable Long id, HttpSession session) {
        if (!isManager(session)) return forbidden();
        User manager = currentUser(session);
        return reCheckinService.reject(id, manager)
                .<ResponseEntity<?>>map(r -> {
                    userRepo.findAll().stream()
                            .filter(u -> u.getEmployee() != null && u.getEmployee().getId().equals(r.getEmployee().getId()))
                            .findFirst()
                            .ifPresent(u -> notificationService.create(u,
                                    "Your re-check-in request has been rejected.", "RECHECKIN_REJECTED", r.getId()));
                    return ResponseEntity.ok(r);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ─── ANNOUNCEMENTS ────────────────────────────────────────────

    @PostMapping("/announcements")
    public ResponseEntity<?> createAnnouncement(@RequestBody Map<String, Object> body, HttpSession session) {
        if (!isManager(session)) return forbidden();
        User manager = currentUser(session);
        Announcement a = announcementService.create(
                manager,
                Announcement.TargetType.valueOf((String) body.getOrDefault("targetType", "ALL")),
                (String) body.get("targetValue"),
                (String) body.get("title"),
                (String) body.get("content"));
        return ResponseEntity.ok(a);
    }

    // ─── BONUS / PENALTY ──────────────────────────────────────────

    @GetMapping("/bonus-penalty")
    public ResponseEntity<?> getTeamBonusPenalty(HttpSession session) {
        if (!isManager(session)) return forbidden();
        Long userId = (Long) session.getAttribute("userId");
        List<Employee> team = employeeRepo.findByManagerUserId(userId);
        List<BonusPenalty> result = new ArrayList<>();
        for (Employee emp : team) {
            result.addAll(bonusPenaltyService.getByEmployee(emp.getId()));
        }
        result.sort(Comparator.comparing(BonusPenalty::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/bonus-penalty")
    public ResponseEntity<?> createBonusPenalty(@RequestBody Map<String, Object> body, HttpSession session) {
        if (!isManager(session)) return forbidden();
        User manager = currentUser(session);
        Long userId = (Long) session.getAttribute("userId");
        Long empId = Long.parseLong(body.get("employeeId").toString());
        return employeeRepo.findById(empId).map(emp -> {
            if (!userId.equals(emp.getManagerUserId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Not your team member"));
            }
            BonusPenalty bp = bonusPenaltyService.create(
                    emp,
                    BonusPenalty.RecordType.valueOf((String) body.get("type")),
                    new BigDecimal(body.get("amount").toString()),
                    (String) body.getOrDefault("reason", ""),
                    Integer.parseInt(body.get("month").toString()),
                    Integer.parseInt(body.get("year").toString()),
                    manager);
            userRepo.findAll().stream()
                    .filter(u -> u.getEmployee() != null && u.getEmployee().getId().equals(empId))
                    .findFirst()
                    .ifPresent(u -> notificationService.create(u,
                            bp.getType() + ": " + bp.getAmount() + " - " + bp.getReason(),
                            bp.getType().name(), bp.getId()));
            return ResponseEntity.ok(bp);
        }).orElse(ResponseEntity.notFound().build());
    }

    // ─── DASHBOARD STATS ──────────────────────────────────────────

    @GetMapping("/stats")
    public ResponseEntity<?> stats(HttpSession session) {
        if (!isManager(session)) return forbidden();
        Long userId = (Long) session.getAttribute("userId");
        List<Employee> team = employeeRepo.findByManagerUserId(userId);
        long pendingLeaves = leaveRepo.findByEmployee_ManagerUserIdAndStatusOrderByRequestDateDesc(
                userId, LeaveRequest.LeaveStatus.PENDING).size();
        long pendingReCheckins = reCheckinService.getPendingForManager(userId).size();
        return ResponseEntity.ok(Map.of(
                "teamSize", team.size(),
                "pendingLeaves", pendingLeaves,
                "pendingReCheckins", pendingReCheckins));
    }
}
