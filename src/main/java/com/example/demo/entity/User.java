/*
 * FILE: User.java
 * PURPOSE: JPA entity representing a login account. Maps to the `users` table.
 *          A User has a role (ADMIN / MANAGER / EMPLOYEE) and optionally links to an Employee
 *          record (admin accounts may have no associated employee).
 *
 * KEY FIELDS:
 *   - username  : Unique login name.
 *   - password  : Stored in plain text in this demo — replace with BCrypt in production.
 *   - role      : ADMIN | MANAGER | EMPLOYEE. Controls which portal and API endpoints are accessible.
 *   - employee  : One-to-one link to Employee; null for pure admin accounts.
 *   - isActive  : Soft-disable flag; inactive users cannot log in.
 *
 * HOW TO MODIFY:
 *   - To enforce password hashing: inject PasswordEncoder in AuthService and hash on save/check.
 *   - To add new roles: extend UserRole and add matching access checks in the interceptor and
 *     every controller that calls session.getAttribute("userRole").
 *   - password is annotated @JsonIgnore so it is never serialised to API responses.
 */
package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "users")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    @JsonIgnore
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
    
    @OneToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;
    
    private Boolean isActive = true;
    
    public enum UserRole {
        ADMIN, MANAGER, EMPLOYEE
    }
}
