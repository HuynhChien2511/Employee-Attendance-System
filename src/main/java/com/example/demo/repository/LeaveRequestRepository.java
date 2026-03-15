/*
 * FILE: LeaveRequestRepository.java
 * PURPOSE: Spring Data JPA repository for LeaveRequest.
 *          Provides queries scoped to employees, status, and manager teams.
 *
 * METHODS:
 *   - findByEmployee(employee)                              : All leave requests for an employee object.
 *   - findByStatus(status)                                  : All requests with a given status (e.g., PENDING).
 *   - findByEmployee_IdOrderByRequestDateDesc(empId)        : Leave list for a specific employee ID,
 *                                                             newest first. Used in employee portal.
 *   - findByEmployee_ManagerUserIdAndStatusOrderByRequestDateDesc(managerUserId, status)
 *       Returns leave requests of team members of a manager filtered by status.
 *       Used for manager approval queue.
 *   - findByEmployee_ManagerUserIdOrderByRequestDateDesc(managerUserId)
 *       All leave requests (any status) for a manager's team.
 *
 * HOW TO MODIFY:
 *   - To add date-range filtering, add a method with startDate/endDate parameters.
 *   - managerUserId refers to User.id of the manager stored in Employee.managerUserId; if
 *     the manager assignment logic changes, update the callers in ManagerApiController.
 */
package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Employee;
import com.example.demo.entity.LeaveRequest;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByEmployee(Employee employee);
    List<LeaveRequest> findByStatus(LeaveRequest.LeaveStatus status);
    List<LeaveRequest> findByEmployee_IdOrderByRequestDateDesc(Long employeeId);
    List<LeaveRequest> findByEmployee_ManagerUserIdAndStatusOrderByRequestDateDesc(Long managerUserId, LeaveRequest.LeaveStatus status);
    List<LeaveRequest> findByEmployee_ManagerUserIdOrderByRequestDateDesc(Long managerUserId);
}
