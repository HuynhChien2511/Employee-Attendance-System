/*
 * FILE: BonusPenaltyService.java
 * PURPOSE: Business logic for managing bonus and penalty records.
 *          Each record is tied to an employee, a month/year period, and an amount.
 *          Used by admin and manager controllers to apply financial adjustments,
 *          and by MonthlySummaryService to compute net salary.
 *
 * METHODS:
 *  - create(employee, type, amount, reason, month, year, createdBy)
 *      Builds and persists a new BonusPenalty record. Sets effectiveDate to the
 *      1st of the given month/year. Called from admin and manager API controllers.
 *
 *  - getByEmployee(employeeId)
 *      Returns all bonus/penalty records for an employee, newest first.
 *      Used on the employee's own financial history view.
 *
 *  - getByEmployeeAndPeriod(employeeId, month, year)
 *      Returns records filtered to a specific month and year.
 *      Used by the monthly summary and period-specific reports.
 *
 *  - getAll()
 *      Returns all bonus/penalty records across all employees, newest first.
 *      Used by the admin overview dashboard.
 *
 *  - delete(id)
 *      Deletes a single bonus/penalty record by its ID.
 *      Called from admin DELETE endpoints.
 *
 *  - sumBonus(employeeId, month, year)
 *      Calls a repository aggregate query to sum all BONUS amounts for the period.
 *      Returns BigDecimal (0 if none). Used by MonthlySummaryService.compute().
 *
 *  - sumPenalty(employeeId, month, year)
 *      Same as sumBonus but for PENALTY type. Returns BigDecimal (0 if none).
 *
 * HOW TO MODIFY:
 *  - To add a new record type: extend the BonusPenalty.RecordType enum and no
 *    changes are needed here; add a dedicated sum method if aggregation is needed.
 *  - To enforce manager-only creation: add a role check in the controller before
 *    calling create(), or add a User authority check here.
 *  - To support currency or multi-currency: add a currency field to BonusPenalty
 *    and update create() and the sum queries accordingly.
 */
package com.example.demo.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.BonusPenalty;
import com.example.demo.entity.Employee;
import com.example.demo.entity.User;
import com.example.demo.repository.BonusPenaltyRepository;

@Service
public class BonusPenaltyService {

    @Autowired
    private BonusPenaltyRepository bonusPenaltyRepository;

    public BonusPenalty create(Employee employee, BonusPenalty.RecordType type, BigDecimal amount,
                               String reason, int month, int year, User createdBy) {
        BonusPenalty bp = new BonusPenalty();
        bp.setEmployee(employee);
        bp.setType(type);
        bp.setAmount(amount);
        bp.setReason(reason);
        bp.setMonth(month);
        bp.setYear(year);
        bp.setEffectiveDate(LocalDate.of(year, month, 1));
        bp.setCreatedBy(createdBy);
        return bonusPenaltyRepository.save(bp);
    }

    public List<BonusPenalty> getByEmployee(Long employeeId) {
        return bonusPenaltyRepository.findByEmployee_IdOrderByCreatedAtDesc(employeeId);
    }

    public List<BonusPenalty> getByEmployeeAndPeriod(Long employeeId, int month, int year) {
        return bonusPenaltyRepository.findByEmployee_IdAndMonthAndYearOrderByCreatedAtDesc(employeeId, month, year);
    }

    public List<BonusPenalty> getAll() {
        return bonusPenaltyRepository.findAllByOrderByCreatedAtDesc();
    }

    public void delete(Long id) {
        bonusPenaltyRepository.deleteById(id);
    }

    public BigDecimal sumBonus(Long employeeId, int month, int year) {
        return bonusPenaltyRepository.sumBonusByEmployeeAndPeriod(employeeId, month, year);
    }

    public BigDecimal sumPenalty(Long employeeId, int month, int year) {
        return bonusPenaltyRepository.sumPenaltyByEmployeeAndPeriod(employeeId, month, year);
    }
}
