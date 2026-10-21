package com.smartemi.validation;

/** Central input limits shared by Bean Validation annotations and the calculation service. */
public final class LoanLimits {
    public static final String MIN_AMOUNT = "1000";
    public static final String MAX_AMOUNT = "1000000000";
    public static final String MIN_RATE = "0";
    public static final String MAX_RATE = "100";
    public static final int MAX_TENURE_MONTHS = 480; // 40 years

    private LoanLimits() { }
}
