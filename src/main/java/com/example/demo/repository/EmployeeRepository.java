/*
 * FILE: EmployeeRepository.java
 * PURPOSE: Spring Data JPA repository for the Employee entity.
 *          Provides lookup methods used by authentication, admin management, and
 *          manager-scoped queries.
 *
 * METHODS:
 *   - findByEmployeeId(employeeId) : Looks up by human-readable employee code (e.g., "EMP001").
 *   - findByEmail(email)           : Used during employee creation to check for duplicates.
 *   - findByManagerUserId(id)      : Returns all employees whose manager is the given User ID.
 *                                    Used to scope manager views to their own team.
 *   - findByDepartment(dept)       : Filters employees by department name.
 *   - findByStatus(status)         : Filters by ACTIVE / INACTIVE / ON_LEAVE.
 *
 * HOW TO MODIFY:
 *   - To search employees by name, add a method such as
 *     findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(...).
 *   - If you add a new filterable field to Employee, add the corresponding repository
 *     method here.
 */
package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmployeeId(String employeeId);
    Optional<Employee> findByEmail(String email);
    List<Employee> findByManagerUserId(Long managerUserId);
    List<Employee> findByDepartment(String department);
    List<Employee> findByStatus(Employee.EmployeeStatus status);
}
