package com.example.demo.repository;

import com.example.demo.entity.AttendanceRecord;
import com.example.demo.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    List<AttendanceRecord> findByEmployee(Employee employee);
    List<AttendanceRecord> findByEmployeeAndCheckInTimeBetween(Employee employee, LocalDateTime start, LocalDateTime end);
}
