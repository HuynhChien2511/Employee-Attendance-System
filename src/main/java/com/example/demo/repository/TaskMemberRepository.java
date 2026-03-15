/*
 * FILE: TaskMemberRepository.java
 * PURPOSE: Spring Data JPA repository for the TaskMember join entity.
 *          Handles querying and deleting task membership records.
 *
 * METHODS:
 *   - findByTask_Id(taskId)                         : Returns all member records for a task.
 *                                                     Used to build the member list in task
 *                                                     detail responses.
 *   - existsByTask_IdAndEmployee_Id(taskId, empId)  : Checks whether a given employee is
 *                                                     a member of a task. Used for access
 *                                                     control in TaskApiController.
 *   - deleteByTask_Id(taskId)                       : Removes all members from a task (e.g.,
 *                                                     when a task is deleted).
 *
 * HOW TO MODIFY:
 *   - To reassign members, delete existing ones with deleteByTask_Id() and re-insert.
 *   - @Transactional is required when calling deleteByTask_Id() from a service/controller;
 *     add @Transactional to the calling method if not already present.
 */
package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.TaskMember;

public interface TaskMemberRepository extends JpaRepository<TaskMember, Long> {

    List<TaskMember> findByTask_Id(Long taskId);

    boolean existsByTask_IdAndEmployee_Id(Long taskId, Long employeeId);

    void deleteByTask_Id(Long taskId);
}
