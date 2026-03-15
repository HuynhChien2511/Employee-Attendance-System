/*
 * FILE: EmployeeService.java
 * PURPOSE: Core CRUD service for the Employee entity. Acts as the single access
 *          point for employee data. Injected into almost every other service and
 *          controller that needs to resolve an employee by ID or employee code.
 *
 * METHODS:
 *  - getAllEmployees()
 *      Returns a list of all Employee records. Used in admin/manager list views
 *      and in services that need to iterate over all staff.
 *
 *  - getEmployeeById(id)
 *      Looks up an employee by their numeric primary key (auto-generated DB id).
 *      Returns Optional<Employee>; callers must handle the empty case.
 *      This is the most frequently called method across the application.
 *
 *  - getEmployeeByEmployeeId(employeeId)
 *      Looks up an employee by their human-readable employee code string
 *      (e.g., "EMP001"). Used during login resolution and import flows.
 *
 *  - saveEmployee(employee)
 *      Persists a new or updated Employee record. Returns the saved entity
 *      (with any generated ID filled in). Called from admin CRUD endpoints.
 *
 *  - deleteEmployee(id)
 *      Deletes the employee by primary key. No soft-delete; removal is permanent.
 *      Called from admin DELETE /api/admin/employees/{id}.
 *
 * HOW TO MODIFY:
 *  - To add soft deletion: add an `isActive` boolean field to Employee, change
 *    deleteEmployee() to set it false and save, and add a filter in getAllEmployees()
 *    to exclude inactive records.
 *  - To add search/filter: add new repository query methods (e.g., findByDepartment)
 *    and expose them here before wiring them to controller endpoints.
 *  - To validate uniqueness (e.g., no duplicate employee codes): add a check in
 *    saveEmployee() using getEmployeeByEmployeeId() before calling save().
 */
package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Employee;
import com.example.demo.repository.EmployeeRepository;

@Service
public class EmployeeService {
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }
    
    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }
    
    public Optional<Employee> getEmployeeByEmployeeId(String employeeId) {
        return employeeRepository.findByEmployeeId(employeeId);
    }
    
    public Employee saveEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }
    
    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
    }
}
