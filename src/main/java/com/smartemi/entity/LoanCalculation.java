package com.smartemi.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_calculations")
public class LoanCalculation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(name = "loan_name", nullable = false, length = 100)
    private String loanName;
    @Enumerated(EnumType.STRING)
    @Column(name = "loan_type", nullable = false, length = 20)
    private LoanType loanType;
    @Column(name = "principal_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal principalAmount;
    @Column(name = "annual_interest_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal annualInterestRate;
    @Column(name = "tenure_months", nullable = false)
    private int tenureMonths;
    @Column(name = "monthly_emi", nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyEmi;
    @Column(name = "total_interest", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalInterest;
    @Column(name = "total_payment", nullable = false, precision = 16, scale = 2)
    private BigDecimal totalPayment;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected LoanCalculation() { }

    public LoanCalculation(User user, String loanName, LoanType loanType, BigDecimal principalAmount,
                           BigDecimal annualInterestRate, int tenureMonths, BigDecimal monthlyEmi,
                           BigDecimal totalInterest, BigDecimal totalPayment) {
        this.user = user; this.loanName = loanName; this.loanType = loanType;
        this.principalAmount = principalAmount; this.annualInterestRate = annualInterestRate;
        this.tenureMonths = tenureMonths; this.monthlyEmi = monthlyEmi;
        this.totalInterest = totalInterest; this.totalPayment = totalPayment;
    }

    @PrePersist void onCreate() { createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getLoanName() { return loanName; }
    public LoanType getLoanType() { return loanType; }
    public BigDecimal getPrincipalAmount() { return principalAmount; }
    public BigDecimal getAnnualInterestRate() { return annualInterestRate; }
    public int getTenureMonths() { return tenureMonths; }
    public BigDecimal getMonthlyEmi() { return monthlyEmi; }
    public BigDecimal getTotalInterest() { return totalInterest; }
    public BigDecimal getTotalPayment() { return totalPayment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
