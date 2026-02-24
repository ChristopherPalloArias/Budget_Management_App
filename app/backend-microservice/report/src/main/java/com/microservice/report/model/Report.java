package com.microservice.report.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "reports")
public class Report {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;
    @Column(name = "user_id", nullable = false)
    private String userId;
    @Column(name = "period", nullable = false, length = 7)
    private String period;
    @Column(name = "total_income", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalIncome;
    @Column(name = "total_expense", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalExpense;
    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public void addIncome(BigDecimal amount) {
        if (amount != null) {
            this.totalIncome = this.totalIncome != null ? this.totalIncome.add(amount) : amount;
            this.recalculateBalance();
        }
    }

    public void addExpense(BigDecimal amount) {
        if (amount != null) {
            this.totalExpense = this.totalExpense != null ? this.totalExpense.add(amount) : amount;
            this.recalculateBalance();
        }
    }

    public void recalculateBalance() {
        BigDecimal income = this.totalIncome != null ? this.totalIncome : BigDecimal.ZERO;
        BigDecimal expense = this.totalExpense != null ? this.totalExpense : BigDecimal.ZERO;
        this.balance = income.subtract(expense);
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
        this.recalculateBalance();
    }

    public void setTotalExpense(BigDecimal totalExpense) {
        this.totalExpense = totalExpense;
        this.recalculateBalance();
    }

    @PrePersist
    public void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
