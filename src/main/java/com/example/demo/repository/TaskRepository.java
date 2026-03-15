/*
 * FILE: TaskRepository.java
 * PURPOSE: Spring Data JPA repository for the Task entity.
 *          Provides standard CRUD operations plus a custom query to fetch tasks
 *          assigned to a specific employee.
 *
 * METHODS:
 *   - findByEmployeeId(employeeId) : Custom JPQL query that joins Task with TaskMember
 *                                     and returns all tasks where the given employee is
 *                                     a member, ordered by creation date descending.
 *                                     Used by TaskApiController.getMyTasks().
 *   (all standard JpaRepository methods: findById, findAll, save, deleteById, etc.)
 *
 * HOW TO MODIFY:
 *   - To filter tasks by status for a given employee, add a new @Query method here with
 *     an additional `AND t.status = :status` clause.
 *   - If the TaskMember join table is renamed, update the JPQL entity name in the query.
 */
package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("SELECT DISTINCT t FROM Task t JOIN TaskMember tm ON tm.task.id = t.id WHERE tm.employee.id = :employeeId ORDER BY t.createdAt DESC")
    List<Task> findByEmployeeId(@Param("employeeId") Long employeeId);
}
