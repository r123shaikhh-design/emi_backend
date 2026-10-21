package com.smartemi.service;

import static org.assertj.core.api.Assertions.*;

import com.smartemi.dto.*;
import com.smartemi.entity.LoanType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class EmiServiceTest {
    private final EmiService svc = new EmiService();

    private EmiResult calc(String p, String rate, int months) {
        return svc.calculate(new BigDecimal(p), new BigDecimal(rate), months, LocalDate.of(2026, 1, 1));
    }

    /** Known value: 100,000 at 12% p.a. for 12 months -> EMI 8,884.88 (r = 0.01, (1.01)^12 = 1.126825). */
    @Test
    void knownValue_oneLakhAt12PercentForOneYear() {
        EmiSummary s = calc("100000", "12", 12).summary();
        assertThat(s.monthlyEmi()).isEqualByComparingTo("8884.88");
        assertThat(s.totalInterest()).isCloseTo(new BigDecimal("6618.56"), within(new BigDecimal("0.15")));
    }

    /** Known value: 10 lakh at 8.5% for 20 years -> roughly 8,678 per month. */
    @Test
    void homeLoan_tenLakhAt8Point5For20Years() {
        assertThat(calc("1000000", "8.5", 240).summary().monthlyEmi())
                .isCloseTo(new BigDecimal("8678"), within(new BigDecimal("1.00")));
    }

    @Test
    void zeroInterest_isPrincipalDividedByMonths() {
        EmiSummary s = calc("120000", "0", 12).summary();
        assertThat(s.monthlyEmi()).isEqualByComparingTo("10000.00");
        assertThat(s.totalInterest()).isEqualByComparingTo("0");
        assertThat(s.totalPayment()).isEqualByComparingTo("120000");
    }

    @Test
    void oneMonthLoan_emiIsPrincipalPlusOneMonthInterest() {
        assertThat(calc("100000", "12", 1).summary().monthlyEmi()).isEqualByComparingTo("101000.00");
    }

    @Test
    void veryLowRate_isSlightlyAbovePlainDivision() {
        assertThat(calc("500000", "0.01", 24).summary().monthlyEmi())
                .isGreaterThan(new BigDecimal("20833.33")).isLessThan(new BigDecimal("20840"));
    }

    @Test
    void veryHighRate_stillAmortizesToZero() {
        EmiResult r = calc("100000", "100", 12);
        assertThat(r.summary().monthlyEmi()).isGreaterThan(new BigDecimal("8333.33"));
        assertThat(r.schedule().getLast().closingBalance()).isEqualByComparingTo("0");
    }

    @Test
    void longTenure_has480RowsAndEndsAtZero() {
        EmiResult r = calc("5000000", "9", 480);
        assertThat(r.schedule()).hasSize(480);
        assertThat(r.schedule().getLast().closingBalance()).isEqualByComparingTo("0");
    }

    @Test
    void finalInstallmentAbsorbsRounding_principalRepaidEqualsLoan() {
        EmiResult r = calc("100000", "0", 3); // 33,333.33 x 2 + 33,333.34
        BigDecimal repaid = r.schedule().stream().map(ScheduleRow::principalPaid).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(repaid).isEqualByComparingTo("100000.00");
        assertThat(r.schedule().getLast().emi()).isEqualByComparingTo("33333.34");
        assertThat(r.schedule().getLast().closingBalance()).isEqualByComparingTo("0.00");
    }

    @Test
    void allMonetaryValuesHaveTwoDecimals() {
        EmiResult r = calc("123457", "7.77", 37);
        r.schedule().forEach(row -> {
            assertThat(row.interestPaid().scale()).isEqualTo(2);
            assertThat(row.closingBalance().scale()).isEqualTo(2);
        });
        assertThat(r.summary().monthlyEmi().scale()).isEqualTo(2);
    }

    @Test
    void invalidInputs_areRejected() {
        assertThatThrownBy(() -> calc("-1", "10", 12)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> calc("0", "10", 12)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> calc("1000", "-0.1", 12)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> calc("1000", "101", 12)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> calc("1000", "10", 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> calc("1000", "10", 481)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void compare_picksLowestEmiAndLowestInterest() {
        List<EmiRequest> loans = List.of(
                EmiRequest.of("1000000", "8.5", 10, TenureUnit.YEARS, LoanType.HOME),
                EmiRequest.of("1000000", "8.5", 20, TenureUnit.YEARS, LoanType.HOME),
                EmiRequest.of("1000000", "8.5", 15, TenureUnit.YEARS, LoanType.HOME));
        CompareResponse c = svc.compare(loans);
        assertThat(c.lowestEmiIndex()).isEqualTo(1);      // longest tenure -> lowest EMI
        assertThat(c.lowestInterestIndex()).isEqualTo(0); // shortest tenure -> least interest
        assertThat(c.tenureInsight()).contains("Option 2").contains("Option 1");
    }

    /** r = 12 / 12 / 100 = 0.01 and (1.01)^12 = 1.126825 - the numbers shown in the "how it was calculated" steps. */
    @Test
    void explain_exposesMonthlyRateAndGrowthFactor() {
        EmiRequest req = EmiRequest.of("100000", "12", 12, TenureUnit.MONTHS, LoanType.PERSONAL);
        CalcExplanation ex = svc.explain(req, svc.calculate(req));
        assertThat(ex.monthlyRate()).isEqualByComparingTo("0.01");
        assertThat(ex.growthFactor()).isEqualByComparingTo("1.126825");
        assertThat(ex.zeroInterest()).isFalse();
        assertThat(ex.first().interestPaid()).isEqualByComparingTo("1000.00");
    }
}
