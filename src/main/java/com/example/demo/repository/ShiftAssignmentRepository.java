package com.example.demo.repository;

import com.example.demo.entity.ShiftAssignment;
import com.example.demo.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, Long> {
    List<ShiftAssignment> findByEmployee(Employee employee);
    List<ShiftAssignment> findByAssignmentDate(LocalDate date);
}
