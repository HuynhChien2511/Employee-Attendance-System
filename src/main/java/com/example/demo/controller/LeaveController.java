/*
 * FILE: LeaveController.java
 * PURPOSE: Legacy REST controller for leave request management at /api/leave.
 *          Provides unauthenticated CRUD and workflow endpoints for leave requests.
 *          NOTE: The main UI leave flow is handled by EmployeeApiController
 *          (submit/cancel) and ManagerApiController/AdminApiController (approve/reject),
 *          all of which enforce session-based authentication.
 *
 * ENDPOINTS:
 *  - GET /api/leave
 *      Returns all leave requests (no filter, no auth check).
 *
 *  - GET /api/leave/pending
 *      Returns only PENDING leave requests.
 *
 *  - GET /api/leave/employee/{employeeId}
 *      Returns all leave requests for a specific employee.
 *
 *  - POST /api/leave
 *      Creates a new leave request from request body. Status is set to PENDING
 *      and requestDate is set to now() inside LeaveService.createLeaveRequest().
 *
 *  - PUT /api/leave/approve/{requestId}?comments=...
 *      Approves a leave request, optionally storing approver comments.
 *      Returns 400 if the request ID is not found.
 *
 *  - PUT /api/leave/reject/{requestId}?comments=...
 *      Rejects a leave request. Returns 400 if not found.
 *
 * HOW TO MODIFY:
 *  - To add authentication: inject HttpSession and check userRole in each handler,
 *    then remove @CrossOrigin.
 *  - To notify employees on approval/rejection: inject NotificationService and call
 *    create() after the status update in the approve/reject handlers.
 *  - To add leave cancellation: add a DELETE /api/leave/{id} or PUT /api/leave/{id}/cancel
 *    endpoint (already implemented in EmployeeApiController as the canonical path).
 */
package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.LeaveRequest;
import com.example.demo.service.LeaveService;

@RestController
@RequestMapping("/api/leave")
@CrossOrigin(origins = "*")
public class LeaveController {
    
    @Autowired
    private LeaveService leaveService;
    
    @GetMapping
    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveService.getAllLeaveRequests();
    }
    
    @GetMapping("/pending")
    public List<LeaveRequest> getPendingLeaveRequests() {
        return leaveService.getPendingLeaveRequests();
    }
    
    @GetMapping("/employee/{employeeId}")
    public List<LeaveRequest> getLeaveRequestsByEmployee(@PathVariable Long employeeId) {
        return leaveService.getLeaveRequestsByEmployee(employeeId);
    }
    
    @PostMapping
    public LeaveRequest createLeaveRequest(@RequestBody LeaveRequest leaveRequest) {
        return leaveService.createLeaveRequest(leaveRequest);
    }
    
    @PutMapping("/approve/{requestId}")
    public ResponseEntity<LeaveRequest> approveLeaveRequest(
            @PathVariable Long requestId, 
            @RequestParam(required = false) String comments) {
        LeaveRequest approved = leaveService.approveLeaveRequest(requestId, comments);
        return approved != null ? ResponseEntity.ok(approved) : ResponseEntity.badRequest().build();
    }
    
    @PutMapping("/reject/{requestId}")
    public ResponseEntity<LeaveRequest> rejectLeaveRequest(
            @PathVariable Long requestId, 
            @RequestParam(required = false) String comments) {
        LeaveRequest rejected = leaveService.rejectLeaveRequest(requestId, comments);
        return rejected != null ? ResponseEntity.ok(rejected) : ResponseEntity.badRequest().build();
    }
}
