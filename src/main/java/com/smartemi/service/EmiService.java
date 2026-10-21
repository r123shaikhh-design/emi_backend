package com.smartemi.service;

import com.smartemi.dto.*;
import com.smartemi.validation.LoanLimits;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Pure EMI mathematics. Rounding policy: intermediate values use 30 significant digits (HALF_UP);
 * every monetary amount is rounded to 2 decimals (HALF_UP). The final installment absorbs rounding
 * differences so the closing balance is exactly 0.00.
 */
@Service
public class EmiService {
    public static final String DISCLAIMER = "Indicative figures only. Actual bank EMIs may vary because of "
            + "processing fees, insurance, floating-rate changes and lender-specific rounding.";

    private static final MathContext MC = new MathContext(30, RoundingMode.HALF_UP);
    private static final int SCALE = 2;
    private static final RoundingMode RM = RoundingMode.HALF_UP;
    private static final BigDecimal MONTHLY_DIVISOR = BigDecimal.valueOf(1200); // 12 months * 100 percent

    public EmiResult calculate(EmiRequest req) {
        return calculate(req.getLoanAmount(), req.getAnnualRate(), req.tenureInMonths(), LocalDate.now());
    }

    /** Calculates EMI summary and full amortization schedule. Installment dates start one month after {@code start}. */
    public EmiResult calculate(BigDecimal principal, BigDecimal annualRate, int months, LocalDate start) {
        if (principal == null || principal.signum() <= 0) {
            throw new IllegalArgumentException("Loan amount must be greater than zero");
        }
        if (annualRate == null || annualRate.signum() < 0 || annualRate.compareTo(new BigDecimal(LoanLimits.MAX_RATE)) > 0) {
            throw new IllegalArgumentException("Interest rate must be between 0 and 100");
        }
        if (months < 1 || months > LoanLimits.MAX_TENURE_MONTHS) {
            throw new IllegalArgumentException("Tenure must be between 1 and " + LoanLimits.MAX_TENURE_MONTHS + " months");
        }
        BigDecimal p = principal.setScale(SCALE, RM);
        BigDecimal r = annualRate.divide(MONTHLY_DIVISOR, MC); // monthly rate = annual / 12 / 100
        BigDecimal emi = monthlyEmi(p, r, months);

        List<ScheduleRow> rows = new ArrayList<>(months);
        BigDecimal balance = p;
        BigDecimal totalInterest = BigDecimal.ZERO.setScale(SCALE);
        for (int i = 1; i <= months; i++) {
            BigDecimal interest = balance.multiply(r).setScale(SCALE, RM);
            BigDecimal principalPart = (i == months) ? balance : emi.subtract(interest);
            BigDecimal payment = principalPart.add(interest);
            BigDecimal closing = balance.subtract(principalPart);
            rows.add(new ScheduleRow(i, start.plusMonths(i), balance, payment, principalPart, interest, closing));
            totalInterest = totalInterest.add(interest);
            balance = closing;
        }
        EmiSummary summary = new EmiSummary(p, annualRate, months, emi, totalInterest, p.add(totalInterest));
        return new EmiResult(summary, rows);
    }

    private BigDecimal monthlyEmi(BigDecimal p, BigDecimal r, int n) {
        if (r.signum() == 0) { // zero-interest loan: no division by (1+r)^n - 1
            return p.divide(BigDecimal.valueOf(n), SCALE, RM);
        }
        BigDecimal growth = BigDecimal.ONE.add(r).pow(n, MC); // (1 + r)^n
        BigDecimal numerator = p.multiply(r).multiply(growth);
        BigDecimal denominator = growth.subtract(BigDecimal.ONE); // > 0 because r > 0
        return numerator.divide(denominator, SCALE, RM);
    }

    /** Recomputes the intermediate values (r and (1+r)^n) so the UI can show how the EMI was formed. */
    public CalcExplanation explain(EmiRequest req, EmiResult res) {
        int n = res.summary().tenureMonths();
        BigDecimal r = res.summary().annualRate().divide(MONTHLY_DIVISOR, MC);
        BigDecimal growth = r.signum() == 0 ? BigDecimal.ONE : BigDecimal.ONE.add(r).pow(n, MC);
        return new CalcExplanation(req.getTenure(), req.getTenureUnit() == TenureUnit.YEARS, n,
                r.setScale(8, RM), growth.setScale(6, RM), res.schedule().getFirst(), res.schedule().getLast(), r.signum() == 0);
    }

    public CompareResponse compare(List<EmiRequest> loans) {
        List<EmiSummary> summaries = loans.stream().map(l -> calculate(l).summary()).toList();
        int lowestEmi = 0, lowestInterest = 0;
        for (int i = 1; i < summaries.size(); i++) {
            if (summaries.get(i).monthlyEmi().compareTo(summaries.get(lowestEmi).monthlyEmi()) < 0) lowestEmi = i;
            if (summaries.get(i).totalInterest().compareTo(summaries.get(lowestInterest).totalInterest()) < 0) lowestInterest = i;
        }
        String insight = "A longer tenure lowers the monthly EMI but increases the total interest you pay; "
                + "a shorter tenure does the opposite.";
        if (lowestEmi != lowestInterest) {
            BigDecimal extra = summaries.get(lowestEmi).totalInterest().subtract(summaries.get(lowestInterest).totalInterest());
            insight = "Option " + (lowestEmi + 1) + " has the lowest EMI, but Option " + (lowestInterest + 1)
                    + " costs " + extra.toPlainString() + " less in total interest. " + insight;
        }
        return new CompareResponse(summaries, lowestEmi, lowestInterest, insight);
    }
}
