package com.smartemi.entity;

/** Loan categories with the display text and sample values used on the calculator's picker page. */
public enum LoanType {
    HOME("Home", "🏠", "Buy or build a house",
         "Home loans usually run 15 to 30 years. Lenders finance only part of the property price, so keep your down payment ready.",
         "5000000", "8.5", 20),
    PERSONAL("Personal", "👤", "Any personal need",
         "Personal loans are usually unsecured, with higher rates and shorter tenures (1 to 5 years). Borrow only what you need.",
         "500000", "12", 3),
    CAR("Car", "🚗", "New or used vehicle",
         "Car loans commonly run 3 to 7 years. Compare dealer finance with bank offers, and remember insurance and registration costs.",
         "800000", "9.25", 5),
    EDUCATION("Education", "🎓", "Fees and living costs",
         "Education loans may have a moratorium while you study, and interest may build up meanwhile. Confirm the terms with your lender.",
         "2500000", "10.5", 10),
    BUSINESS("Business", "💼", "Grow your business",
         "Check processing fees and any collateral. Keep your EMI comfortably below your monthly business cash flow.",
         "2000000", "13", 5),
    CUSTOM("Custom", "⚙️", "Enter your own terms",
         "Enter your own terms. Any amount, rate and tenure within the allowed limits works.",
         "100000", "10", 2);

    private final String label, icon, blurb, tip, defaultAmount, defaultRate;
    private final int defaultYears;

    LoanType(String label, String icon, String blurb, String tip, String defaultAmount, String defaultRate, int defaultYears) {
        this.label = label; this.icon = icon; this.blurb = blurb; this.tip = tip;
        this.defaultAmount = defaultAmount; this.defaultRate = defaultRate; this.defaultYears = defaultYears;
    }

    public String getLabel() { return label; }
    public String getIcon() { return icon; }
    public String getBlurb() { return blurb; }
    public String getTip() { return tip; }
    public String getDefaultAmount() { return defaultAmount; }
    public String getDefaultRate() { return defaultRate; }
    public int getDefaultYears() { return defaultYears; }
}
