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

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.security.reset-token-expiry-minutes:15}")
    private long resetTokenExpiryMinutes;

    @Value("${app.security.reset-password.mail-from:no-reply@eas.local}")
    private String mailFrom;

    @Value("${app.public-base-url:}")
    private String publicBaseUrl;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    /**
     * Validates credentials and returns the matched active user.
     * Passwords are stored in plain text in this demo; use BCrypt in production.
     */
    public Optional<User> login(String username, String password) {
        if (username == null || password == null) return Optional.empty();
        return userRepository.findByUsername(username.trim())
                .filter(u -> Boolean.TRUE.equals(u.getIsActive()) && password.equals(u.getPassword()));
    }

    public void requestPasswordReset(String email, String appBaseUrl) {
        if (email == null || email.isBlank()) return;

        if (isMailConfigIncomplete()) {
            log.warn("Password reset requested for {} but SMTP config is incomplete. host='{}', username set={}",
                    email.trim(), mailHost, mailUsername != null && !mailUsername.isBlank());
            return;
        }

        userRepository.findByEmployee_EmailIgnoreCase(email.trim())
                .filter(u -> Boolean.TRUE.equals(u.getIsActive()))
                .ifPresent(user -> {
                    String token = UUID.randomUUID().toString().replace("-", "")
                            + UUID.randomUUID().toString().replace("-", "");
                    user.setResetPasswordToken(token);
                    user.setResetPasswordExpiresAt(LocalDateTime.now().plusMinutes(resetTokenExpiryMinutes));
                    userRepository.save(user);

                    String resetLink = buildResetLink(token, appBaseUrl);
                    try {
                        sendResetEmail(email.trim(), resetLink);
                        log.info("Password reset email queued for {}", email.trim());
                    } catch (RuntimeException ex) {
                        log.error("Failed to send password reset email to {}", email.trim(), ex);
                        // Keep API response generic and avoid keeping an unusable token when mail send fails.
                        user.setResetPasswordToken(null);
                        user.setResetPasswordExpiresAt(null);
                        userRepository.save(user);
                    }
                });
    }

    private boolean isMailConfigIncomplete() {
        return mailHost == null || mailHost.isBlank()
                || mailUsername == null || mailUsername.isBlank();
    }

    private String buildResetLink(String token, String appBaseUrl) {
        String base = (publicBaseUrl != null && !publicBaseUrl.isBlank()) ? publicBaseUrl : appBaseUrl;
        return base + "/reset-password?token=" + token;
    }

    private void sendResetEmail(String toEmail, String resetLink) {
        SimpleMailMessage msg = new SimpleMailMessage();
        String fromAddress = (mailFrom != null && !mailFrom.isBlank()) ? mailFrom : mailUsername;
        msg.setFrom(fromAddress);
        msg.setTo(toEmail);
        msg.setSubject("Password reset request");
        msg.setText("We received a request to reset your password.\n\n"
                + "Use the link below to set a new password:\n"
                + resetLink + "\n\n"
                + "This link expires soon and can only be used once.\n"
                + "If you did not request this, please ignore this email.");
        mailSender.send(msg);
    }

    public void resetPassword(String token, String newPassword) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Reset token is required");
        }
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters");
        }

        User user = userRepository.findByResetPasswordToken(token.trim())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token"));

        LocalDateTime expiresAt = user.getResetPasswordExpiresAt();
        if (expiresAt == null || expiresAt.isBefore(LocalDateTime.now())) {
            user.setResetPasswordToken(null);
            user.setResetPasswordExpiresAt(null);
            userRepository.save(user);
            throw new IllegalArgumentException("Invalid or expired reset token");
        }

        user.setPassword(newPassword);
        user.setResetPasswordToken(null);
        user.setResetPasswordExpiresAt(null);
        userRepository.save(user);
    }
}
