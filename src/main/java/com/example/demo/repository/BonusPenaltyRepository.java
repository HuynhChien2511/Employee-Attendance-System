/*
 * FILE: BonusPenaltyRepository.java
 * PURPOSE: Spring Data JPA repository for BonusPenalty records.
 *          Includes aggregate JPQL queries that sum bonuses and penalties for a given
 *          employee and pay period, used by MonthlySummaryService.
 *
 * METHODS:
 *   - findByEmployee_IdOrderByCreatedAtDesc(empId)    : All records for an employee.
 *   - findByEmployee_IdAndMonthAndYearOrderByCreatedAtDesc(...)
 *       Records for a specific month/year (used in bonus/penalty list views).
 *   - findAllByOrderByCreatedAtDesc()                  : All records across all employees
 *                                                        (admin view).
 *   - sumBonusByEmployeeAndPeriod(empId, month, year)  : Sum of BONUS amounts for a period.
 *       Returns 0 (via COALESCE) when there are no matching rows.
 *   - sumPenaltyByEmployeeAndPeriod(empId, month, year): Sum of PENALTY amounts for a period.
 *
 * HOW TO MODIFY:
 *   - If the RecordType enum values change, update the string literals 'BONUS'/'PENALTY'
 *     inside the @Query annotations here.
 *   - To add a bulk-delete for a pay period, add a @Modifying @Query method.
 */
package com.example.demo.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.BonusPenalty;

public interface BonusPenaltyRepository extends JpaRepository<BonusPenalty, Long> {
    List<BonusPenalty> findByEmployee_IdOrderByCreatedAtDesc(Long employeeId);
    List<BonusPenalty> findByEmployee_IdAndMonthAndYearOrderByCreatedAtDesc(Long employeeId, Integer month, Integer year);
    List<BonusPenalty> findAllByOrderByCreatedAtDesc();

    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM BonusPenalty b WHERE b.employee.id = :empId AND b.month = :month AND b.year = :year AND b.type = 'BONUS'")
    BigDecimal sumBonusByEmployeeAndPeriod(@Param("empId") Long employeeId, @Param("month") Integer month, @Param("year") Integer year);

    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM BonusPenalty b WHERE b.employee.id = :empId AND b.month = :month AND b.year = :year AND b.type = 'PENALTY'")
    BigDecimal sumPenaltyByEmployeeAndPeriod(@Param("empId") Long employeeId, @Param("month") Integer month, @Param("year") Integer year);
}
