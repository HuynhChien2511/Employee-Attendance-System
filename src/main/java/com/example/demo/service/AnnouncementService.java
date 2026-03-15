/*
 * FILE: AnnouncementService.java
 * PURPOSE: Business logic layer for company announcements.
 *          Handles creating, retrieving, and filtering announcements based on the
 *          target audience (ALL, DEPARTMENT, ROLE, or specific employee ID).
 *
 * METHODS:
 *  - create(sender, targetType, targetValue, title, content)
 *      Creates and persists a new announcement. Called by admin/manager controllers
 *      when posting a new announcement via POST /api/admin/announcements or
 *      POST /api/manager/announcements.
 *
 *  - getAll()
 *      Returns all announcements ordered by creation date (newest first).
 *      Used by admin endpoints to display the full announcement board.
 *
 *  - getVisibleToEmployee(emp)
 *      Filters announcements that a specific employee should see: those targeting
 *      ALL employees, the employee's department, or that employee's ID specifically.
 *      Called by GET /api/employee/announcements.
 *
 *  - getVisibleToManager(managerUser, team)
 *      Returns announcements relevant to a manager: announcements for their team's
 *      department, ALL employees, their team members by ID, or announcements they
 *      created themselves. Called by GET /api/manager/announcements.
 *
 * HOW TO MODIFY:
 *  - To add a new target type: add the value to Announcement.TargetType enum, then
 *    update the JPQL queries in AnnouncementRepository and add filtering logic here.
 *  - To support rich text or attachments: extend the Announcement entity fields and
 *    update the create() method signature accordingly.
 *  - To add pagination: change return types to Page<Announcement> and add Pageable
 *    parameters, then update callers in the controller layer.
 */
package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Announcement;
import com.example.demo.entity.Employee;
import com.example.demo.entity.User;
import com.example.demo.repository.AnnouncementRepository;

@Service
public class AnnouncementService {

    @Autowired
    private AnnouncementRepository announcementRepository;

    public Announcement create(User sender, Announcement.TargetType targetType, String targetValue, String title, String content) {
        Announcement a = new Announcement();
        a.setSender(sender);
        a.setTargetType(targetType);
        a.setTargetValue(targetValue);
        a.setTitle(title);
        a.setContent(content);
        return announcementRepository.save(a);
    }

    public List<Announcement> getAll() {
        return announcementRepository.findAllByOrderByCreatedAtDesc();
    }

    /** Returns announcements visible to a given employee (role=EMPLOYEE). */
    public List<Announcement> getVisibleToEmployee(Employee emp) {
        return announcementRepository.findVisibleToEmployee(
                emp.getDepartment() != null ? emp.getDepartment() : "",
                String.valueOf(emp.getId()));
    }

    /** Returns announcements visible to a manager (role=MANAGER): their team + ALL + sent by them. */
    public List<Announcement> getVisibleToManager(User managerUser, List<Employee> team) {
        List<String> empIdStrs = team.stream().map(e -> String.valueOf(e.getId())).toList();
        String dept = team.isEmpty() ? "" : (team.get(0).getDepartment() != null ? team.get(0).getDepartment() : "");
        return announcementRepository.findVisibleToManager(dept, empIdStrs, managerUser.getId());
    }
}
