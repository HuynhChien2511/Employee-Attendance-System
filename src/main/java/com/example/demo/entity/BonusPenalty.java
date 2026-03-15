/*
 * FILE: BonusPenalty.java
 * PURPOSE: JPA entity representing a bonus or penalty record for an employee in a given
 *          month/year. Maps to the `bonus_penalty` table.
 *
 * KEY FIELDS:
 *   - type         : BONUS | PENALTY (RecordType enum).
 *   - amount       : Monetary amount (positive BigDecimal).
 *   - month / year : The pay period this record applies to.
 *   - effectiveDate: Set to the first day of the given month.
 *   - createdBy    : The admin/manager who issued the bonus or penalty.
 *
 * HOW TO MODIFY:
 *   - The BonusPenaltyRepository contains aggregate queries (sumBonus, sumPenalty) used by
 *     MonthlySummaryService; if you change the RecordType enum values update those queries.
 *   - To issue a bonus/penalty, call BonusPenaltyService.create() — never persist directly
 *     to avoid bypassing the service layer logic.
 */
package com.example.demo.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "bonus_penalty")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class BonusPenalty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecordType type;

    @Column(nullable = false)
    private BigDecimal amount;

    private String reason;
    private LocalDate effectiveDate;
    private Integer month;
    private Integer year;

    @ManyToOne
    @JoinColumn(name = "created_by")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User createdBy;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public enum RecordType {
        BONUS, PENALTY
    }
}
