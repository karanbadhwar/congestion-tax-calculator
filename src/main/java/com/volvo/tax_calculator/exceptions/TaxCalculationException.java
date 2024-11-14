package com.volvo.tax_calculator.exceptions;

public class TaxCalculationException extends RuntimeException {
    public TaxCalculationException(String message) {
        super(message);
    }
}
