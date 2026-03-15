/*
 * FILE: ShiftRepository.java
 * PURPOSE: Spring Data JPA repository for the Shift entity.
 *
 * METHODS:
 *   - findByIsActive(isActive) : Returns active (true) or retired (false) shifts.
 *                                Used to populate shift selection dropdowns.
 *
 * HOW TO MODIFY:
 *   - To add search by name, add findByShiftNameContainingIgnoreCase(String name).
 *   - Always prefer setting isActive=false over deleting a shift to preserve
 *     historical ShiftAssignment records.
 */
package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Shift;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {
    List<Shift> findByIsActive(Boolean isActive);
}
