package com.smartemi.dto;

import com.smartemi.entity.LoanType;
import java.math.BigDecimal;

public record LoanTypeInfo(String code, String label, String icon, String blurb, String tip,
                           BigDecimal defaultAmount, BigDecimal defaultRate, int defaultYears) {
    public static LoanTypeInfo from(LoanType t) {
        return new LoanTypeInfo(t.name(), t.getLabel(), t.getIcon(), t.getBlurb(), t.getTip(),
                new BigDecimal(t.getDefaultAmount()), new BigDecimal(t.getDefaultRate()), t.getDefaultYears());
    }
}
