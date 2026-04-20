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
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final long RATE_LIMIT_WINDOW_MS = 15 * 60 * 1000L;
    private static final int FORGOT_MAX_ATTEMPTS = 5;
    private static final int RESET_MAX_ATTEMPTS = 10;

    private final Map<String, Queue<Long>> forgotAttemptsByIp = new ConcurrentHashMap<>();
    private final Map<String, Queue<Long>> resetAttemptsByIp = new ConcurrentHashMap<>();

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

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body,
                                            HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        if (isRateLimited(forgotAttemptsByIp, ip, FORGOT_MAX_ATTEMPTS, RATE_LIMIT_WINDOW_MS)) {
            return ResponseEntity.status(429).body(Map.of("error", "Too many requests. Please try again later."));
        }

        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }

        String appBaseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        authService.requestPasswordReset(email, appBaseUrl);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "If this email exists, a reset link will be sent.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body,
                                           HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        if (isRateLimited(resetAttemptsByIp, ip, RESET_MAX_ATTEMPTS, RATE_LIMIT_WINDOW_MS)) {
            return ResponseEntity.status(429).body(Map.of("error", "Too many requests. Please try again later."));
        }

        String token = body.get("token");
        String newPassword = body.get("newPassword");
        try {
            authService.resetPassword(token, newPassword);
            return ResponseEntity.ok(Map.of("message", "Password has been reset successfully"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
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

    private boolean isRateLimited(Map<String, Queue<Long>> bucket,
                                  String key,
                                  int maxAttempts,
                                  long windowMs) {
        long now = System.currentTimeMillis();
        Queue<Long> timestamps = bucket.computeIfAbsent(key, k -> new LinkedList<>());
        synchronized (timestamps) {
            while (!timestamps.isEmpty() && now - timestamps.peek() > windowMs) {
                timestamps.poll();
            }
            if (timestamps.size() >= maxAttempts) {
                return true;
            }
            timestamps.offer(now);
            return false;
        }
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
