/*
 * FILE: ReCheckinRequestService.java
 * PURPOSE: Handles employee requests to correct/add a missed check-in record.
 *          When approved, automatically creates a new AttendanceRecord for the
 *          requested date and links it back to the request.
 *
 * METHODS:
 *  - submit(employee, requestedDate, requestedCheckinTime, reason)
 *      Creates and saves a new ReCheckinRequest with status PENDING.
 *      Called by POST /api/employee/recheckin when an employee missed clocking in.
 *
 *  - getByEmployee(employeeId)
 *      Returns all re-check-in requests submitted by a specific employee,
 *      newest first. Used on the employee request history view.
 *
 *  - getPendingForManager(managerUserId)
 *      Returns PENDING requests for employees whose managerUserId matches.
 *      Used by the manager's approval queue.
 *
 *  - getAllForManager(managerUserId)
 *      Returns all requests (any status) for the manager's team.
 *      Used on the manager's full request history view.
 *
 *  - getAllPending()
 *      Returns all PENDING requests across all teams. Used by admin oversight.
 *
 *  - approve(id, approver)
 *      Sets status = APPROVED, records approver + timestamp, then creates a new
 *      AttendanceRecord (status PRESENT) for the requested date/time with a note.
 *      Links the record back to the request before saving both. Returns Optional.
 *
 *  - reject(id, approver)
 *      Sets status = REJECTED, records approver + timestamp, and saves.
 *      Does NOT create an attendance record. Returns Optional.
 *
 * HOW TO MODIFY:
 *  - To notify the employee on approval/rejection: inject NotificationService and
 *    call create() inside both approve() and reject().
 *  - To allow the employee to cancel a PENDING request: add a cancel(id, employeeId)
 *    method that verifies ownership before deleting or setting status = CANCELLED.
 *  - To validate that the requested date is not in the future: add a date check
 *    inside submit() before saving.
 */
package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.AttendanceRecord;
import com.example.demo.entity.Employee;
import com.example.demo.entity.ReCheckinRequest;
import com.example.demo.entity.User;
import com.example.demo.repository.AttendanceRecordRepository;
import com.example.demo.repository.ReCheckinRequestRepository;

@Service
public class ReCheckinRequestService {

    @Autowired
    private ReCheckinRequestRepository reCheckinRepo;

    @Autowired
    private AttendanceRecordRepository attendanceRepo;

    public ReCheckinRequest submit(Employee employee, LocalDate requestedDate,
                                   LocalDateTime requestedCheckinTime, String reason) {
        ReCheckinRequest r = new ReCheckinRequest();
        r.setEmployee(employee);
        r.setRequestedDate(requestedDate);
        r.setRequestedCheckinTime(requestedCheckinTime);
        r.setReason(reason);
        r.setStatus(ReCheckinRequest.RequestStatus.PENDING);
        return reCheckinRepo.save(r);
    }

    public List<ReCheckinRequest> getByEmployee(Long employeeId) {
        return reCheckinRepo.findByEmployee_IdOrderByCreatedAtDesc(employeeId);
    }

    public List<ReCheckinRequest> getPendingForManager(Long managerUserId) {
        return reCheckinRepo.findByEmployee_ManagerUserIdAndStatusOrderByCreatedAtDesc(
                managerUserId, ReCheckinRequest.RequestStatus.PENDING);
    }

    public List<ReCheckinRequest> getAllForManager(Long managerUserId) {
        return reCheckinRepo.findByEmployee_ManagerUserIdOrderByCreatedAtDesc(managerUserId);
    }

    public List<ReCheckinRequest> getAllPending() {
        return reCheckinRepo.findByStatusOrderByCreatedAtDesc(ReCheckinRequest.RequestStatus.PENDING);
    }

    public Optional<ReCheckinRequest> approve(Long id, User approver) {
        return reCheckinRepo.findById(id).map(r -> {
            r.setStatus(ReCheckinRequest.RequestStatus.APPROVED);
            r.setApprovedBy(approver);
            r.setApprovedAt(LocalDateTime.now());
            // Create an attendance record for the requested date/time if not exists
            AttendanceRecord record = new AttendanceRecord();
            record.setEmployee(r.getEmployee());
            record.setCheckInTime(r.getRequestedCheckinTime());
            record.setStatus(AttendanceRecord.AttendanceStatus.PRESENT);
            record.setNotes("Re-check-in approved by " + approver.getUsername());
            attendanceRepo.save(record);
            r.setAttendanceRecord(record);
            return reCheckinRepo.save(r);
        });
    }

    public Optional<ReCheckinRequest> reject(Long id, User approver) {
        return reCheckinRepo.findById(id).map(r -> {
            r.setStatus(ReCheckinRequest.RequestStatus.REJECTED);
            r.setApprovedBy(approver);
            r.setApprovedAt(LocalDateTime.now());
            return reCheckinRepo.save(r);
        });
    }
}
