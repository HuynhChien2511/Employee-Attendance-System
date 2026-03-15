/*
 * FILE: TaskMember.java
 * PURPOSE: JPA entity that represents the many-to-many relationship between Task and Employee.
 *          Each row in the `task_members` table means one employee is assigned to one task.
 *
 * FIELDS:
 *   - id       : Auto-generated primary key.
 *   - task     : The task this member belongs to (lazy-loaded).
 *   - employee : The employee who is a member of that task (lazy-loaded).
 *
 * HOW TO MODIFY:
 *   - To add extra membership metadata (e.g., role in the task, join date), add the field here
 *     and update TaskApiController.createTask() where TaskMember objects are built.
 *   - To remove all members of a task call TaskMemberRepository.deleteByTask_Id().
 */
package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "task_members")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TaskMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Employee employee;
}
