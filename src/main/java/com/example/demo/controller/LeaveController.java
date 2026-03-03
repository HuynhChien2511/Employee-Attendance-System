package com.example.demo.controller;

import com.example.demo.entity.LeaveRequest;
import com.example.demo.service.LeaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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
