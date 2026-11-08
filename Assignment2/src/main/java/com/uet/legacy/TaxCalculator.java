package com.uet.legacy;

public class TaxCalculator {

    private static final double DEFAULT_RATE = 0.15;
    private static final double PK_RATE = 0.17;
    private static final double US_RATE = 0.08;
    private static final double UK_RATE = 0.20;

    public double rateFor(String country) {
        if (country.equals("PK")) {
            return PK_RATE;
        } else if (country.equals("US")) {
            return US_RATE;
        } else if (country.equals("UK")) {
            return UK_RATE;
        }
        return DEFAULT_RATE;
    }
}