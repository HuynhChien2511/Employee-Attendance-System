/*
 * FILE: TaskSubmissionRepository.java
 * PURPOSE: Spring Data JPA repository for the TaskSubmission entity.
 *          Retrieves submission records for tasks and for individual employees.
 *
 * METHODS:
 *   - findByTask_IdOrderBySubmittedAtDesc(taskId)
 *       Returns all submissions for a task, newest first. Used in task detail views.
 *   - findByTask_IdAndEmployee_IdOrderBySubmittedAtDesc(taskId, employeeId)
 *       Returns a specific employee's submissions for a task. Useful for checking if
 *       an employee has already submitted.
 *
 * HOW TO MODIFY:
 *   - To limit the number of submissions returned, use Spring Data's Pageable parameter
 *     or add a LIMIT clause via @Query.
 *   - To add reviewer grading (mark a submission as reviewed), add a status/grade field
 *     to TaskSubmission and expose a PUT endpoint in TaskApiController.
 */
package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.TaskSubmission;

public interface TaskSubmissionRepository extends JpaRepository<TaskSubmission, Long> {

    List<TaskSubmission> findByTask_IdOrderBySubmittedAtDesc(Long taskId);

    List<TaskSubmission> findByTask_IdAndEmployee_IdOrderBySubmittedAtDesc(Long taskId, Long employeeId);
}
