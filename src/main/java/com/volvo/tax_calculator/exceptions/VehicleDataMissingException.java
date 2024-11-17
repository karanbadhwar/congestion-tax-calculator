package com.volvo.tax_calculator.exceptions;

public class VehicleDataMissingException extends RuntimeException {
    public VehicleDataMissingException(String message) {
        super(message);
    }
}