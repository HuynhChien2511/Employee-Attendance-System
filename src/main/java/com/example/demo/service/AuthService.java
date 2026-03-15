/*
 * FILE: AuthService.java
 * PURPOSE: Authentication logic for user login. Validates username and password
 *          against the users table and returns the active User record on success.
 *
 * METHODS:
 *  - login(username, password)
 *      Looks up the user by username (case-sensitive, trimmed), then checks:
 *        1. The user exists.
 *        2. user.isActive == true.
 *        3. The provided password matches the stored password (plain-text in this demo).
 *      Returns Optional.empty() on any failure; returns Optional<User> on success.
 *      Called by AuthController (POST /login) to establish the session.
 *
 * HOW TO MODIFY:
 *  - To enable password hashing: add BCryptPasswordEncoder as a @Bean in a
 *    SecurityConfig class, inject it here, and replace the plain-text comparison
 *    with encoder.matches(password, user.getPassword()). Also hash passwords when
 *    creating or updating users.
 *  - To add account lockout: track failed attempts (e.g., a counter field on User)
 *    and throw or return empty after N consecutive failures.
 *  - To support case-insensitive usernames: change UserRepository.findByUsername()
 *    to use a LOWER() comparison, or use Spring's Collation settings.
 */
package com.example.demo.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Validates credentials and returns the matched active user.
     * Passwords are stored in plain text in this demo; use BCrypt in production.
     */
    public Optional<User> login(String username, String password) {
        if (username == null || password == null) return Optional.empty();
        return userRepository.findByUsername(username.trim())
                .filter(u -> Boolean.TRUE.equals(u.getIsActive()) && password.equals(u.getPassword()));
    }
}
