/*
 * FILE: UserRepository.java
 * PURPOSE: Spring Data JPA repository for the User entity.
 *          Provides login lookup and standard CRUD operations.
 *
 * METHODS:
 *   - findByUsername(username) : Returns the User with a matching username, used during
 *                                login in AuthService.login(). Returns Optional.empty() if
 *                                no match.
 *
 * HOW TO MODIFY:
 *   - To support login by email (instead of username), add
 *     findByEmployee_Email(String email) here and update AuthService.
 *   - To search users by role, add findByRole(User.UserRole role) and call from
 *     admin management APIs.
 */
package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
