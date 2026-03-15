/*
 * FILE: AnnouncementRepository.java
 * PURPOSE: Spring Data JPA repository for the Announcement entity.
 *          Includes custom JPQL queries to filter announcements by visibility scope.
 *
 * METHODS:
 *   - findAllByOrderByCreatedAtDesc()    : Returns every announcement (admin view),
 *                                          newest first.
 *   - findVisibleToEmployee(dept, empId) : Returns announcements where targetType is ALL,
 *                                          or DEPARTMENT matching the employee's dept,
 *                                          or EMPLOYEE matching the employee's ID string.
 *   - findVisibleToManager(dept, empIds, senderId)
 *                                        : Same as above plus any announcement sent by
 *                                          the manager themselves.
 *
 * HOW TO MODIFY:
 *   - If a new TargetType is added to Announcement.TargetType, add a corresponding OR
 *     clause to both JPQL queries here and update AnnouncementService methods.
 *   - targetValue stores the employee ID as a String; ensure the calling code converts
 *     `employee.getId()` to a String before querying.
 */
package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.Announcement;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findAllByOrderByCreatedAtDesc();

    @Query("SELECT a FROM Announcement a WHERE a.targetType = 'ALL' " +
           "OR (a.targetType = 'DEPARTMENT' AND a.targetValue = :dept) " +
           "OR (a.targetType = 'EMPLOYEE' AND a.targetValue = :empId) " +
           "ORDER BY a.createdAt DESC")
    List<Announcement> findVisibleToEmployee(@Param("dept") String department,
                                              @Param("empId") String employeeIdStr);

    @Query("SELECT a FROM Announcement a WHERE a.targetType = 'ALL' " +
           "OR (a.targetType = 'DEPARTMENT' AND a.targetValue = :dept) " +
           "OR (a.targetType = 'EMPLOYEE' AND a.targetValue IN :empIds) " +
           "OR a.sender.id = :senderId " +
           "ORDER BY a.createdAt DESC")
    List<Announcement> findVisibleToManager(@Param("dept") String department,
                                             @Param("empIds") List<String> empIds,
                                             @Param("senderId") Long senderId);
}
