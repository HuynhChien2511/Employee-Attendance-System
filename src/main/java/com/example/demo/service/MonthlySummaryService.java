/*
 * FILE: MonthlySummaryService.java
 * PURPOSE: Computes a complete monthly payroll and attendance summary for one
 *          employee for a given month and year. Aggregates attendance record counts,
 *          total hours, bonus/penalty sums, and derives net salary.
 *          Used by AdminApiController and EmployeeApiController for salary/report views.
 *
 * METHODS:
 *  - compute(employeeId, month, year)
 *      Main entry point. Steps performed:
 *        1. Loads the Employee (throws IllegalArgumentException if not found).
 *        2. Queries all attendance records in [start of month .. end of month].
 *        3. Counts days by status: PRESENT/LATE → presentDays, HALF_DAY → halfDays,
 *           ABSENT → absentDays, ON_LEAVE → leaveDays.
 *        4. Sums total hours worked across PRESENT records (rounded to 1 decimal).
 *        5. Loads baseSalary from Employee (defaults to 0 if null).
 *        6. Calls BonusPenaltyService.sumBonus() and sumPenalty() for the period.
 *        7. Calculates netSalary = baseSalary + totalBonus - totalPenalty.
 *        8. Returns a Map<String, Object> with all fields plus the raw attendanceRecords
 *           list for detailed drill-down rendering.
 *
 * HOW TO MODIFY:
 *  - To add overtime pay: count hours beyond a threshold (e.g., > 8h/day) and add
 *    an overtimePay field to the returned map.
 *  - To include leave type breakdown (sick/annual/etc.): group leaveDays further by
 *    LeaveRequest.leaveType using a separate query.
 *  - To cache results: annotate compute() with @Cacheable and invalidate on new
 *    attendance or bonus/penalty saves.
 *  - To expose as a report download (PDF/Excel): add a new method that calls
 *    compute() and passes the result to a reporting library.
 */
package com.example.demo.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.AttendanceRecord;
import com.example.demo.entity.Employee;
import com.example.demo.repository.AttendanceRecordRepository;

@Service
public class MonthlySummaryService {

    @Autowired
    private AttendanceRecordRepository attendanceRepo;

    @Autowired
    private BonusPenaltyService bonusPenaltyService;

    @Autowired
    private EmployeeService employeeService;

    /**
     * Computes a monthly summary for the given employee, month, and year.
     */
    public Map<String, Object> compute(Long employeeId, int month, int year) {
        Employee employee = employeeService.getEmployeeById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.atEndOfMonth().atTime(23, 59, 59);

        List<AttendanceRecord> records = attendanceRepo
                .findByEmployee_IdAndCheckInTimeBetweenOrderByCheckInTimeDesc(employeeId, start, end);

        long presentDays = records.stream()
                .filter(r -> r.getStatus() == AttendanceRecord.AttendanceStatus.PRESENT
                          || r.getStatus() == AttendanceRecord.AttendanceStatus.LATE)
                .count();
        long halfDays = records.stream()
                .filter(r -> r.getStatus() == AttendanceRecord.AttendanceStatus.HALF_DAY)
                .count();
        long absentDays = records.stream()
                .filter(r -> r.getStatus() == AttendanceRecord.AttendanceStatus.ABSENT)
                .count();
        long leaveDays = records.stream()
                .filter(r -> r.getStatus() == AttendanceRecord.AttendanceStatus.ON_LEAVE)
                .count();

        double totalHours = records.stream()
                .filter(r -> r.getHoursWorked() != null)
                .mapToDouble(AttendanceRecord::getHoursWorked)
                .sum();

        BigDecimal baseSalary = employee.getBaseSalary() != null ? employee.getBaseSalary() : BigDecimal.ZERO;
        BigDecimal totalBonus = bonusPenaltyService.sumBonus(employeeId, month, year);
        BigDecimal totalPenalty = bonusPenaltyService.sumPenalty(employeeId, month, year);
        BigDecimal netSalary = baseSalary.add(totalBonus).subtract(totalPenalty);

        Map<String, Object> summary = new HashMap<>();
        summary.put("employeeId", employeeId);
        summary.put("employeeName", employee.getFirstName() + " " + employee.getLastName());
        summary.put("employeeCode", employee.getEmployeeId());
        summary.put("department", employee.getDepartment());
        summary.put("month", month);
        summary.put("year", year);
        summary.put("presentDays", presentDays);
        summary.put("halfDays", halfDays);
        summary.put("absentDays", absentDays);
        summary.put("leaveDays", leaveDays);
        summary.put("totalHours", Math.round(totalHours * 10.0) / 10.0);
        summary.put("baseSalary", baseSalary);
        summary.put("totalBonus", totalBonus);
        summary.put("totalPenalty", totalPenalty);
        summary.put("netSalary", netSalary);
        summary.put("attendanceRecords", records);
        return summary;
    }
}
