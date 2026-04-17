/*
 * FILE: TaskApiController.java
 * PURPOSE: Session-authenticated REST controller for task management and task file transfer.
 *          Serves both employee task actions and admin/manager task administration.
 *          Handles task listing, detail retrieval, submissions, task creation, finish action,
 *          and secure file download for task attachments and submission files.
 *
 * HELPER METHODS:
 *  - currentUser(session)
 *      Resolves the currently logged-in User from session.userId.
 *  - buildTaskSummary(task)
 *      Builds the compact task JSON used by list endpoints.
 *  - updateMissedDeadlines(tasks)
 *      Converts IN_PROCESS tasks to MISSED_DEADLINE if deadline is before today.
 *
 * ENDPOINTS — EMPLOYEE:
 *  - GET /api/employee/tasks
 *      Returns all tasks assigned to the current employee.
 *
 *  - GET /api/employee/tasks/{id}
 *      Returns full task detail including description, attached file, members,
 *      and submission history. Access allowed to task members, ADMIN, and MANAGER.
 *
 *  - POST /api/employee/tasks/{id}/submit
 *      Submits text and/or a file for the given task. Only task members can submit.
 *      Stores files under uploadDir/submissions with filename sanitization and
 *      path-traversal protection.
 *
 * ENDPOINTS — ADMIN/MANAGER:
 *  - GET /api/tasks
 *      Returns all tasks in the system. Restricted to ADMIN and MANAGER roles.
 *
 *  - POST /api/tasks
 *      Creates a new task with title, description, deadline, assigned memberIds,
 *      and optional attachment. Stores attachments under uploadDir/tasks.
 *
 *  - PUT /api/tasks/{id}/finish
 *      Marks a task as FINISHED.
 *
 * ENDPOINTS — FILES:
 *  - GET /api/files/download?path=...
 *      Securely downloads an uploaded file for an authenticated user. Resolves the
 *      path under uploadDir and blocks traversal outside the upload root.
 *
 * HOW TO MODIFY:
 *  - To add task editing or deletion: add PUT/DELETE endpoints here and update the
 *    Task/TaskMember repositories consistently.
 *  - To send notifications on assignment or submission: inject NotificationService
 *    and create notifications after task creation and submission save.
 *  - To support multiple attachments: introduce a TaskAttachment entity rather than
 *    storing a single attachedFileName/attachedFilePath pair on Task.
 */
package com.example.demo.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.entity.Employee;
import com.example.demo.entity.Task;
import com.example.demo.entity.TaskMember;
import com.example.demo.entity.TaskSubmission;
import com.example.demo.entity.User;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.TaskMemberRepository;
import com.example.demo.repository.TaskRepository;
import com.example.demo.repository.TaskSubmissionRepository;
import com.example.demo.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@RestController
public class TaskApiController {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Autowired private UserRepository userRepo;
    @Autowired private TaskRepository taskRepo;
    @Autowired private TaskMemberRepository taskMemberRepo;
    @Autowired private TaskSubmissionRepository taskSubmissionRepo;
    @Autowired private EmployeeRepository employeeRepo;

    private User currentUser(HttpSession s) {
        Long id = (Long) s.getAttribute("userId");
        return id == null ? null : userRepo.findById(id).orElse(null);
    }

    // ── Employee: list my tasks ───────────────────────────────────

    @GetMapping("/api/employee/tasks")
    public ResponseEntity<?> getMyTasks(HttpSession session) {
        User user = currentUser(session);
        if (user == null) return ResponseEntity.status(401).build();
        Employee emp = user.getEmployee();
        if (emp == null) return ResponseEntity.ok(List.of());

        List<Task> tasks = taskRepo.findByEmployeeId(emp.getId());
        updateMissedDeadlines(tasks);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Task t : tasks) {
            result.add(buildTaskSummary(t));
        }
        return ResponseEntity.ok(result);
    }

    // ── Employee: task detail ─────────────────────────────────────

    @GetMapping("/api/employee/tasks/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getTaskDetail(@PathVariable Long id, HttpSession session) {
        User user = currentUser(session);
        if (user == null) return ResponseEntity.status(401).build();
        Employee emp = user.getEmployee();

        Task task = taskRepo.findById(id).orElse(null);
        if (task == null) return ResponseEntity.notFound().build();

        // Check access: must be a member or admin/manager
        String role = (String) session.getAttribute("userRole");
        if (emp != null && !taskMemberRepo.existsByTask_IdAndEmployee_Id(id, emp.getId())) {
            if (!"ADMIN".equals(role) && !"MANAGER".equals(role)) {
                return ResponseEntity.status(403).build();
            }
        }

        List<TaskSubmission> submissions = taskSubmissionRepo.findByTask_IdOrderBySubmittedAtDesc(id);
        List<Map<String, Object>> subList = new ArrayList<>();
        for (TaskSubmission sub : submissions) {
            Map<String, Object> sm = new LinkedHashMap<>();
            sm.put("id", sub.getId());
            sm.put("employeeId", sub.getEmployee() != null ? sub.getEmployee().getId() : null);
            sm.put("employeeName", sub.getEmployee() != null
                    ? sub.getEmployee().getFirstName() + " " + sub.getEmployee().getLastName() : null);
            sm.put("textContent", sub.getTextContent());
            sm.put("fileName", sub.getFileName());
            sm.put("filePath", sub.getFilePath());
            sm.put("submittedAt", sub.getSubmittedAt());
            subList.add(sm);
        }

        List<Map<String, Object>> members = new ArrayList<>();
        for (TaskMember m : taskMemberRepo.findByTask_Id(id)) {
            Map<String, Object> mm = new LinkedHashMap<>();
            mm.put("employeeId", m.getEmployee() != null ? m.getEmployee().getId() : null);
            mm.put("employeeName", m.getEmployee() != null
                    ? m.getEmployee().getFirstName() + " " + m.getEmployee().getLastName() : null);
            members.add(mm);
        }

        Map<String, Object> detail = buildTaskSummary(task);
        detail.put("description", task.getDescription());
        detail.put("attachedFileName", task.getAttachedFileName());
        detail.put("attachedFilePath", task.getAttachedFilePath());
        detail.put("createdBy", task.getCreatedBy() != null ? task.getCreatedBy().getUsername() : null);
        detail.put("submissions", subList);
        detail.put("members", members);
        return ResponseEntity.ok(detail);
    }

    // ── Employee: submit task ─────────────────────────────────────

    @PostMapping("/api/employee/tasks/{id}/submit")
    public ResponseEntity<?> submitTask(
            @PathVariable Long id,
            @RequestParam(value = "textContent", required = false) String textContent,
            @RequestParam(value = "file", required = false) MultipartFile file,
            HttpSession session) throws IOException {

        User user = currentUser(session);
        if (user == null) return ResponseEntity.status(401).build();
        Employee emp = user.getEmployee();
        if (emp == null) return ResponseEntity.badRequest().body(Map.of("error", "No employee record"));

        Task task = taskRepo.findById(id).orElse(null);
        if (task == null) return ResponseEntity.notFound().build();

        if (!taskMemberRepo.existsByTask_IdAndEmployee_Id(id, emp.getId())) {
            return ResponseEntity.status(403).body(Map.of("error", "Not a member of this task"));
        }

        TaskSubmission sub = new TaskSubmission();
        sub.setTask(task);
        sub.setEmployee(emp);
        sub.setTextContent(textContent);

        if (file != null && !file.isEmpty()) {
            String originalFilename = Objects.requireNonNullElse(file.getOriginalFilename(), "file");
            // Sanitize: only keep safe characters
            String safeName = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
            String storedName = System.currentTimeMillis() + "_" + safeName;
            Path uploadPath = Paths.get(uploadDir, "submissions");
            Files.createDirectories(uploadPath);
            // Prevent path traversal
            Path dest = uploadPath.resolve(storedName).normalize();
            if (!dest.startsWith(uploadPath.normalize())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid file name"));
            }
            Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
            sub.setFileName(originalFilename);
            sub.setFilePath("submissions/" + storedName);
        }

        taskSubmissionRepo.save(sub);
        return ResponseEntity.ok(Map.of("message", "Submitted successfully"));
    }

    // ── Admin/Manager: list all tasks ─────────────────────────────

    @GetMapping("/api/tasks")
    public ResponseEntity<?> getAllTasks(HttpSession session) {
        User user = currentUser(session);
        if (user == null) return ResponseEntity.status(401).build();
        String role = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(role) && !"MANAGER".equals(role)) return ResponseEntity.status(403).build();

        List<Task> tasks = taskRepo.findAll();
        updateMissedDeadlines(tasks);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Task t : tasks) {
            result.add(buildTaskSummary(t));
        }
        return ResponseEntity.ok(result);
    }

    // ── Admin/Manager: create task ────────────────────────────────

    @PostMapping("/api/tasks")
    public ResponseEntity<?> createTask(
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("deadline") String deadline,
            @RequestParam("memberIds") List<Long> memberIds,
            @RequestParam(value = "file", required = false) MultipartFile file,
            HttpSession session) throws IOException {

        User user = currentUser(session);
        if (user == null) return ResponseEntity.status(401).build();
        String role = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(role) && !"MANAGER".equals(role)) return ResponseEntity.status(403).build();

        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setDeadline(LocalDate.parse(deadline));
        task.setStatus(Task.TaskStatus.IN_PROCESS);
        task.setCreatedBy(user);
        taskRepo.save(task);

        for (Long empId : memberIds) {
            employeeRepo.findById(empId).ifPresent(emp -> {
                TaskMember tm = new TaskMember();
                tm.setTask(task);
                tm.setEmployee(emp);
                taskMemberRepo.save(tm);
            });
        }

        if (file != null && !file.isEmpty()) {
            String originalFilename = Objects.requireNonNullElse(file.getOriginalFilename(), "file");
            String safeName = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
            String storedName = System.currentTimeMillis() + "_" + safeName;
            Path uploadPath = Paths.get(uploadDir, "tasks");
            Files.createDirectories(uploadPath);
            Path dest = uploadPath.resolve(storedName).normalize();
            if (!dest.startsWith(uploadPath.normalize())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid file name"));
            }
            Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
            task.setAttachedFileName(originalFilename);
            task.setAttachedFilePath("tasks/" + storedName);
            taskRepo.save(task);
        }

        return ResponseEntity.ok(buildTaskSummary(task));
    }

    // ── Admin/Manager: mark task finished ────────────────────────

    @PutMapping("/api/tasks/{id}/finish")
    public ResponseEntity<?> finishTask(@PathVariable Long id, HttpSession session) {
        User user = currentUser(session);
        if (user == null) return ResponseEntity.status(401).build();
        String role = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(role) && !"MANAGER".equals(role)) return ResponseEntity.status(403).build();

        Task task = taskRepo.findById(id).orElse(null);
        if (task == null) return ResponseEntity.notFound().build();
        task.setStatus(Task.TaskStatus.FINISHED);
        taskRepo.save(task);
        return ResponseEntity.ok(Map.of("message", "Task marked as finished"));
    }

    // ── File download ─────────────────────────────────────────────

    @GetMapping("/api/files/download")
    public ResponseEntity<Resource> downloadFile(
            @RequestParam("path") String filePath,
            HttpSession session) {

        if (currentUser(session) == null) return ResponseEntity.status(401).build();

        try {
            Path basePath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path resolvedPath = basePath.resolve(filePath).normalize();
            // Prevent path traversal attack
            if (!resolvedPath.startsWith(basePath)) {
                return ResponseEntity.status(403).build();
            }
            Resource resource = new UrlResource(resolvedPath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }
            String filename = resolvedPath.getFileName().toString();
            // Strip timestamp prefix for display name
            if (filename.matches("\\d+_.*")) {
                filename = filename.substring(filename.indexOf('_') + 1);
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename.replace("\"", "") + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ── Helpers ───────────────────────────────────────────────────

    private Map<String, Object> buildTaskSummary(Task task) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", task.getId());
        m.put("title", task.getTitle());
        m.put("deadline", task.getDeadline());
        m.put("status", task.getStatus());
        m.put("createdAt", task.getCreatedAt());
        m.put("memberCount", taskMemberRepo.findByTask_Id(task.getId()).size());
        return m;
    }

    private void updateMissedDeadlines(List<Task> tasks) {
        LocalDate today = LocalDate.now();
        for (Task t : tasks) {
            if (t.getStatus() == Task.TaskStatus.IN_PROCESS
                    && t.getDeadline() != null
                    && t.getDeadline().isBefore(today)) {
                t.setStatus(Task.TaskStatus.MISSED_DEADLINE);
                taskRepo.save(t);
            }
        }
    }
}
