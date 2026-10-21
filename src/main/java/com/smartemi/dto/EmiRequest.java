package com.smartemi.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smartemi.entity.LoanType;
import com.smartemi.validation.LoanLimits;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/** Input for EMI calculation (JSON body for the API, query/form parameters for pages). */
public class EmiRequest {
    @NotNull(message = "Loan amount is required")
    @DecimalMin(value = LoanLimits.MIN_AMOUNT, message = "Loan amount must be at least 1,000")
    @DecimalMax(value = LoanLimits.MAX_AMOUNT, message = "Loan amount must not exceed 1,000,000,000")
    private BigDecimal loanAmount;

    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = LoanLimits.MIN_RATE, message = "Interest rate cannot be negative")
    @DecimalMax(value = LoanLimits.MAX_RATE, message = "Interest rate must not exceed 100%")
    private BigDecimal annualRate;

    @NotNull(message = "Tenure is required")
    @Min(value = 1, message = "Tenure must be at least 1")
    @Max(value = 10000, message = "Tenure is too large")
    private Integer tenure;

    @NotNull(message = "Tenure unit is required")
    private TenureUnit tenureUnit = TenureUnit.YEARS;

    @NotNull(message = "Loan type is required")
    private LoanType loanType = LoanType.PERSONAL;

    @Size(max = 100, message = "Loan name must be at most 100 characters")
    private String loanName;

    public static EmiRequest of(String amount, String rate, int tenure, TenureUnit unit, LoanType type) {
        EmiRequest r = new EmiRequest();
        r.loanAmount = new BigDecimal(amount); r.annualRate = new BigDecimal(rate);
        r.tenure = tenure; r.tenureUnit = unit; r.loanType = type;
        return r;
    }

    @JsonIgnore
    @AssertTrue(message = "Tenure must be between 1 month and 40 years")
    public boolean isTenureWithinLimits() {
        return tenure == null || tenureUnit == null || tenureInMonths() <= LoanLimits.MAX_TENURE_MONTHS;
    }

    @JsonIgnore
    public int tenureInMonths() {
        return tenureUnit == TenureUnit.YEARS ? tenure * 12 : tenure;
    }

    public BigDecimal getLoanAmount() { return loanAmount; }
    public void setLoanAmount(BigDecimal v) { this.loanAmount = v; }
    public BigDecimal getAnnualRate() { return annualRate; }
    public void setAnnualRate(BigDecimal v) { this.annualRate = v; }
    public Integer getTenure() { return tenure; }
    public void setTenure(Integer v) { this.tenure = v; }
    public TenureUnit getTenureUnit() { return tenureUnit; }
    public void setTenureUnit(TenureUnit v) { this.tenureUnit = v; }
    public LoanType getLoanType() { return loanType; }
    public void setLoanType(LoanType v) { this.loanType = v; }
    public String getLoanName() { return loanName; }
    public void setLoanName(String v) { this.loanName = v; }
}
