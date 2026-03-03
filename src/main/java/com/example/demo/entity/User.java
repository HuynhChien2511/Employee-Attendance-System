package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
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
