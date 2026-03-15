/*
 * FILE: SharedApiController.java
 * PURPOSE: Session-authenticated REST controller for features shared across all roles
 *          at /api/shared. Provides a unified API for announcements, suggestions
 *          (inbox/sent/reply), notifications, and the managers list.
 *          The controller reads userRole from session to return role-filtered data.
 *
 * HELPER:
 *  - currentUser(session)  Resolves User by session userId. Returns null if not logged in.
 *
 * ENDPOINTS — ANNOUNCEMENTS:
 *  - GET /api/shared/announcements
 *      Returns announcements filtered by the caller's role:
 *      ADMIN → all; MANAGER → visible to their team; EMPLOYEE → visible to them.
 *
 * ENDPOINTS — SUGGESTIONS:
 *  - GET /api/shared/suggestions/inbox
 *      Returns all suggestions received by the current user.
 *
 *  - GET /api/shared/suggestions/sent
 *      Returns all suggestions sent by the current user.
 *
 *  - POST /api/shared/suggestions  { receiverId, subject, message }
 *      Sends a new suggestion to the specified user and creates a notification
 *      for the receiver.
 *
 *  - PUT /api/shared/suggestions/{id}/reply  { reply }
 *      Saves a reply text on a suggestion. Returns 404 if not found.
 *
 *  - PUT /api/shared/suggestions/{id}/read
 *      Marks a suggestion as read by the receiver.
 *
 * ENDPOINTS — NOTIFICATIONS:
 *  - GET /api/shared/notifications
 *      Returns all notifications for the current user.
 *
 *  - GET /api/shared/notifications/count
 *      Returns { count: N } for unread notifications. Polled by the frontend.
 *
 *  - PUT /api/shared/notifications/{id}/read
 *      Marks a single notification as read.
 *
 *  - PUT /api/shared/notifications/read-all
 *      Marks all notifications as read for the current user.
 *
 * ENDPOINTS — UTILITIES:
 *  - GET /api/shared/managers
 *      Returns active users with MANAGER or ADMIN role. Used in the "send suggestion
 *      to" dropdown in the employee UI.
 *
 * HOW TO MODIFY:
 *  - To add a new shared feature (e.g., polls, events): add the service injection
 *    and new @GetMapping/@PostMapping methods here following the same session-validation
 *    pattern.
 *  - To restrict suggestion replies to managers only: add a role check in the
 *    replySuggestion() handler before calling suggestionService.reply().
 *  - To add pagination to notifications/announcements: change return types to
 *    Page<T> and add Pageable parameters.
 */
package com.example.demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Employee;
import com.example.demo.entity.Suggestion;
import com.example.demo.entity.User;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AnnouncementService;
import com.example.demo.service.NotificationService;
import com.example.demo.service.SuggestionService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/shared")
public class SharedApiController {

    @Autowired private UserRepository userRepo;
    @Autowired private EmployeeRepository employeeRepo;
    @Autowired private AnnouncementService announcementService;
    @Autowired private SuggestionService suggestionService;
    @Autowired private NotificationService notificationService;

    private User currentUser(HttpSession s) {
        Long id = (Long) s.getAttribute("userId");
        return id == null ? null : userRepo.findById(id).orElse(null);
    }

    // ─── ANNOUNCEMENTS ────────────────────────────────────────────

    @GetMapping("/announcements")
    public ResponseEntity<?> getMyAnnouncements(HttpSession session) {
        User user = currentUser(session);
        if (user == null) return ResponseEntity.status(401).build();

        String role = (String) session.getAttribute("userRole");
        if ("ADMIN".equals(role)) {
            return ResponseEntity.ok(announcementService.getAll());
        } else if ("MANAGER".equals(role)) {
            List<Employee> team = user.getId() != null
                    ? employeeRepo.findByManagerUserId(user.getId())
                    : List.of();
            return ResponseEntity.ok(announcementService.getVisibleToManager(user, team));
        } else {
            Employee emp = user.getEmployee();
            if (emp == null) return ResponseEntity.ok(List.of());
            return ResponseEntity.ok(announcementService.getVisibleToEmployee(emp));
        }
    }

    // ─── SUGGESTIONS ─────────────────────────────────────────────

    @GetMapping("/suggestions/inbox")
    public ResponseEntity<?> getSuggestionInbox(HttpSession session) {
        User user = currentUser(session);
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(suggestionService.getInbox(user.getId()));
    }

    @GetMapping("/suggestions/sent")
    public ResponseEntity<?> getSuggestionSent(HttpSession session) {
        User user = currentUser(session);
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(suggestionService.getSent(user.getId()));
    }

    @PostMapping("/suggestions")
    public ResponseEntity<?> sendSuggestion(@RequestBody Map<String, Object> body, HttpSession session) {
        User sender = currentUser(session);
        if (sender == null) return ResponseEntity.status(401).build();

        Long receiverId = Long.parseLong(body.get("receiverId").toString());
        return userRepo.findById(receiverId).<ResponseEntity<?>>map(receiver -> {
            Suggestion s = suggestionService.send(
                    sender, receiver,
                    (String) body.getOrDefault("subject", ""),
                    (String) body.get("message"));
            // Notify receiver
            notificationService.create(receiver,
                    "New suggestion from " + sender.getUsername() + ": " + s.getSubject(),
                    "SUGGESTION", s.getId());
            return ResponseEntity.ok(s);
        }).orElse(ResponseEntity.badRequest().body(Map.of("error", "Receiver not found")));
    }

    @PutMapping("/suggestions/{id}/reply")
    public ResponseEntity<?> replySuggestion(@PathVariable Long id,
                                              @RequestBody Map<String, String> body,
                                              HttpSession session) {
        User user = currentUser(session);
        if (user == null) return ResponseEntity.status(401).build();
        Suggestion updated = suggestionService.reply(id, body.get("reply"));
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/suggestions/{id}/read")
    public ResponseEntity<?> markSuggestionRead(@PathVariable Long id, HttpSession session) {
        if (currentUser(session) == null) return ResponseEntity.status(401).build();
        suggestionService.markRead(id);
        return ResponseEntity.ok(Map.of("message", "Marked read"));
    }

    // ─── NOTIFICATIONS ────────────────────────────────────────────

    @GetMapping("/notifications")
    public ResponseEntity<?> getNotifications(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(notificationService.getAll(userId));
    }

    @GetMapping("/notifications/count")
    public ResponseEntity<?> countUnread(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(Map.of("count", notificationService.countUnread(userId)));
    }

    @PutMapping("/notifications/{id}/read")
    public ResponseEntity<?> markNotificationRead(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).build();
        notificationService.markRead(id);
        return ResponseEntity.ok(Map.of("message", "Marked read"));
    }

    @PutMapping("/notifications/read-all")
    public ResponseEntity<?> markAllRead(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).build();
        notificationService.markAllRead(userId);
        return ResponseEntity.ok(Map.of("message", "All marked read"));
    }

    // ─── MANAGERS LIST (for suggestion "send to" dropdown) ────────

    @GetMapping("/managers")
    public ResponseEntity<?> getManagers(HttpSession session) {
        if (session.getAttribute("userId") == null) return ResponseEntity.status(401).build();
        List<Map<String, Object>> managers = userRepo.findAll().stream()
                .filter(u -> u.getRole() == User.UserRole.MANAGER || u.getRole() == User.UserRole.ADMIN)
                .filter(u -> Boolean.TRUE.equals(u.getIsActive()))
                .map(u -> {
                    java.util.Map<String, Object> m = new java.util.HashMap<>();
                    m.put("id", u.getId());
                    m.put("username", u.getUsername());
                    m.put("role", u.getRole().name());
                    if (u.getEmployee() != null) {
                        m.put("name", u.getEmployee().getFirstName() + " " + u.getEmployee().getLastName());
                    } else {
                        m.put("name", u.getUsername());
                    }
                    return m;
                }).toList();
        return ResponseEntity.ok(managers);
    }
}
