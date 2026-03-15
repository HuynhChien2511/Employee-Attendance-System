/*
 * FILE: EmployeeController.java
 * PURPOSE: Legacy REST controller for employee CRUD at /api/employees.
 *          Provides unauthenticated access to employee records.
 *          NOTE: Authorised employee management (with admin role checks) is handled
 *          by AdminApiController at /api/admin/employees.
 *
 * ENDPOINTS:
 *  - GET /api/employees
 *      Returns all employee records.
 *
 *  - GET /api/employees/{id}
 *      Returns a single employee by their DB ID. Returns 404 if not found.
 *
 *  - POST /api/employees
 *      Creates a new employee from request body. No user account is created here.
 *
 *  - PUT /api/employees/{id}
 *      Updates an existing employee. Validates that the employee exists first.
 *      Returns 404 if not found.
 *
 *  - DELETE /api/employees/{id}
 *      Permanently deletes an employee record by ID. No soft-delete.
 *
 * HOW TO MODIFY:
 *  - To add authentication/role checks: inject HttpSession and mirror the pattern
 *    used in AdminApiController.isAdmin(), then remove @CrossOrigin.
 *  - To create an associated user account when creating an employee: inject
 *    UserRepository and replicate the user-creation logic from
 *    AdminApiController.createEmployee().
 *  - To add pagination: change List<Employee> return type to Page<Employee> and
 *    add a Pageable parameter to getAllEmployees().
 */
package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Employee;
import com.example.demo.service.EmployeeService;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "*")
public class EmployeeController {
    
    @Autowired
    private EmployeeService employeeService;
    
    @GetMapping
    public List<Employee> getAllEmployees() {
        return employeeService.getAllEmployees();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        return employeeService.getEmployeeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public Employee createEmployee(@RequestBody Employee employee) {
        return employeeService.saveEmployee(employee);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id, @RequestBody Employee employee) {
        return employeeService.getEmployeeById(id)
                .map(existing -> {
                    employee.setId(id);
                    return ResponseEntity.ok(employeeService.saveEmployee(employee));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok().build();
    }
}
