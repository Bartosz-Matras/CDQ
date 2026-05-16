package com.bartoszmatras.cdq.model;

public enum TransactionCategory {
    GROCERIES,
    SALARY,
    RENT,
    UTILITIES,
    HOME,
    TRANSPORT,
    CAR,
    HEALTHCARE,
    BEAUTY,
    ENTERTAINMENT,
    TRAVEL,
    EDUCATION,
    INSURANCE,
    INVESTMENTS,
    CLOTHING,
    TRANSFERS,
    BANKING,
    OTHER;

    public static boolean isValid(String value) {
        if (value == null || value.isBlank())
            return false;
        try {
            valueOf(value.trim().toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}