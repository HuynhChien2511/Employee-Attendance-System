/*
 * FILE: ReCheckinRequestRepository.java
 * PURPOSE: Spring Data JPA repository for ReCheckinRequest.
 *          Provides queries scoped to individual employees, approval status, and
 *          the manager's team.
 *
 * METHODS:
 *   - findByEmployee_IdOrderByCreatedAtDesc(empId)          : All re-checkin requests for an employee.
 *   - findByStatusOrderByCreatedAtDesc(status)              : All requests system-wide with a given status
 *                                                             (used by admin to find all PENDING).
 *   - findByEmployee_IdAndStatusOrderByCreatedAtDesc(...)   : Requests for an employee filtered by status.
 *   - findByEmployee_ManagerUserIdAndStatusOrderByCreatedAtDesc(managerUserId, status)
 *       Returns pending requests for all employees managed by a specific manager.
 *   - findByEmployee_ManagerUserIdOrderByCreatedAtDesc(managerUserId)
 *       All re-checkin requests (any status) for a manager's team.
 *
 * HOW TO MODIFY:
 *   - All manager-scoped queries rely on Employee.managerUserId matching User.id; update
 *     the queries if the manager-employee relationship structure changes.
 */
package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.ReCheckinRequest;

public interface ReCheckinRequestRepository extends JpaRepository<ReCheckinRequest, Long> {
    List<ReCheckinRequest> findByEmployee_IdOrderByCreatedAtDesc(Long employeeId);
    List<ReCheckinRequest> findByStatusOrderByCreatedAtDesc(ReCheckinRequest.RequestStatus status);
    List<ReCheckinRequest> findByEmployee_IdAndStatusOrderByCreatedAtDesc(Long employeeId, ReCheckinRequest.RequestStatus status);

    // Pending requests for employees managed by a specific manager (by manager's userId)
    List<ReCheckinRequest> findByEmployee_ManagerUserIdAndStatusOrderByCreatedAtDesc(Long managerUserId, ReCheckinRequest.RequestStatus status);
    List<ReCheckinRequest> findByEmployee_ManagerUserIdOrderByCreatedAtDesc(Long managerUserId);
}
