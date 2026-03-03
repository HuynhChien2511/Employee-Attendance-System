package com.example.demo.service;

import com.example.demo.entity.LeaveRequest;
import com.example.demo.entity.Employee;
import com.example.demo.repository.LeaveRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
