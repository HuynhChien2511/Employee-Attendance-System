/*
 * FILE: AuthController.java
 * PURPOSE: REST controller handling authentication at the /api/auth prefix.
 *          Manages login, logout, and session validation. Sets/clears session attributes
 *          used by AuthInterceptor to protect all /api/** routes.
 *
 * ENDPOINTS:
 *  - POST /api/auth/login  { username, password }
 *      Delegates to AuthService.login(). On success, stores userId/userRole/username
 *      in the HttpSession and returns user info JSON.
 *      Returns 401 if credentials are invalid.
 *
 *  - POST /api/auth/logout
 *      Invalidates the entire session. Called when user clicks the logout button.
 *      Returns a success message.
 *
 *  - GET /api/auth/me
 *      Returns the current logged-in user's info (id, username, role, employee fields).
 *      Used by the frontend on page load to restore session state.
 *      Returns 401 if session has no userId.
 *
 *  - buildUserInfo(user)  [private helper]
 *      Builds the standard user info map returned by /login and /me.
 *      Includes employee fields (firstName, lastName, dept) if an employee record
 *      is linked, or defaults to ("Admin", "") if not.
 *
 * HOW TO MODIFY:
 *  - To add password hashing support: update the credential check in AuthService.login()
 *    rather than changing this controller.
 *  - To add a "remember me" / persistent session: configure server.servlet.session.timeout
 *    in application.properties, or implement a token-based approach.
 *  - To return additional profile data on login: extend buildUserInfo() with new fields
 *    from User or Employee.
 */
package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AuthService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpSession session) {
        String username = body.get("username");
        String password = body.get("password");

        return authService.login(username, password)
                .<ResponseEntity<?>>map(user -> {
                    session.setAttribute("userId", user.getId());
                    session.setAttribute("userRole", user.getRole().name());
                    session.setAttribute("username", user.getUsername());
                    return ResponseEntity.ok(buildUserInfo(user));
                })
                .orElse(ResponseEntity.status(401).body(Map.of("error", "Invalid username or password")));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }
        return userRepository.findById(userId)
                .<ResponseEntity<?>>map(user -> ResponseEntity.ok(buildUserInfo(user)))
                .orElse(ResponseEntity.status(401).body(Map.of("error", "Session invalid")));
    }

    private Map<String, Object> buildUserInfo(User user) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", user.getId());
        info.put("username", user.getUsername());
        info.put("role", user.getRole().name());
        if (user.getEmployee() != null) {
            info.put("employeeId", user.getEmployee().getId());
            info.put("employeeCode", user.getEmployee().getEmployeeId());
            info.put("firstName", user.getEmployee().getFirstName());
            info.put("lastName", user.getEmployee().getLastName());
            info.put("department", user.getEmployee().getDepartment());
        } else {
            info.put("firstName", "Admin");
            info.put("lastName", "");
        }
        return info;
    }
}
