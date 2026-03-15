/*
 * FILE: LeaveService.java
 * PURPOSE: Business logic for employee leave requests (apply, approve, reject).
 *          Manages the lifecycle of a LeaveRequest from PENDING → APPROVED/REJECTED.
 *          Delegates employee lookups to EmployeeService.
 *
 * METHODS:
 *  - getAllLeaveRequests()
 *      Returns every leave request in the system. Used by admin for full oversight.
 *
 *  - getPendingLeaveRequests()
 *      Returns only requests with status PENDING. Used by admin/manager approval
 *      screens to show the action-required queue.
 *
 *  - getLeaveRequestsByEmployee(employeeId)
 *      Returns all leave requests submitted by a specific employee.
 *      Called from the employee's own leave history view.
 *
 *  - createLeaveRequest(leaveRequest)
 *      Sets requestDate = now() and status = PENDING, then saves the record.
 *      Called from POST /api/employee/leave.
 *
 *  - approveLeaveRequest(requestId, comments)
 *      Sets status = APPROVED, approvalDate = now(), stores approver comments, and
 *      saves. Returns null if the request ID is not found.
 *      Called from admin/manager approval endpoints.
 *
 *  - rejectLeaveRequest(requestId, comments)
 *      Same as approveLeaveRequest but sets status = REJECTED.
 *      Called from admin/manager rejection endpoints.
 *
 * HOW TO MODIFY:
 *  - To track who approved/rejected: add an `approvedBy` User field to LeaveRequest
 *    and pass the approver User into approveLeaveRequest()/rejectLeaveRequest().
 *  - To enforce leave balance (days remaining): add a `leaveBalance` field to
 *    Employee, deduct on approval, and refund on rejection.
 *  - To notify the employee on decision: inject NotificationService and call
 *    notificationService.create() inside the approve/reject methods.
 */
package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Employee;
import com.example.demo.entity.LeaveRequest;
import com.example.demo.repository.LeaveRequestRepository;

@Service
public class LeaveService {
    
    @Autowired
    private LeaveRequestRepository leaveRequestRepository;
    
    @Autowired
    private EmployeeService employeeService;
    
    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveRequestRepository.findAll();
    }
    
    public List<LeaveRequest> getPendingLeaveRequests() {
        return leaveRequestRepository.findByStatus(LeaveRequest.LeaveStatus.PENDING);
    }
    
    public List<LeaveRequest> getLeaveRequestsByEmployee(Long employeeId) {
        Optional<Employee> employee = employeeService.getEmployeeById(employeeId);
        return employee.map(leaveRequestRepository::findByEmployee).orElse(List.of());
    }
    
    public LeaveRequest createLeaveRequest(LeaveRequest leaveRequest) {
        leaveRequest.setRequestDate(LocalDateTime.now());
        leaveRequest.setStatus(LeaveRequest.LeaveStatus.PENDING);
        return leaveRequestRepository.save(leaveRequest);
    }
    
    public LeaveRequest approveLeaveRequest(Long requestId, String comments) {
        Optional<LeaveRequest> request = leaveRequestRepository.findById(requestId);
        if (request.isPresent()) {
            LeaveRequest leaveRequest = request.get();
            leaveRequest.setStatus(LeaveRequest.LeaveStatus.APPROVED);
            leaveRequest.setApprovalDate(LocalDateTime.now());
            leaveRequest.setApproverComments(comments);
            return leaveRequestRepository.save(leaveRequest);
        }
        return null;
    }
    
    public LeaveRequest rejectLeaveRequest(Long requestId, String comments) {
        Optional<LeaveRequest> request = leaveRequestRepository.findById(requestId);
        if (request.isPresent()) {
            LeaveRequest leaveRequest = request.get();
            leaveRequest.setStatus(LeaveRequest.LeaveStatus.REJECTED);
            leaveRequest.setApprovalDate(LocalDateTime.now());
            leaveRequest.setApproverComments(comments);
            return leaveRequestRepository.save(leaveRequest);
        }
        return null;
    }
}
