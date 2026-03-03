package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "employees")
public class Employee {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String firstName;
    
    @Column(nullable = false)
    private String lastName;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(unique = true, nullable = false)
    private String employeeId;
    
    private String phone;
    
    private String department;
    
    private String position;
    
    private LocalDate hireDate;
    
    @Enumerated(EnumType.STRING)
    private EmployeeStatus status;
    
    public enum EmployeeStatus {
        ACTIVE, INACTIVE, ON_LEAVE
    }
}
