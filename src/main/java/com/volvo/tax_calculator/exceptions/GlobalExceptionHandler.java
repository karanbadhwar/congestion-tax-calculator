package com.volvo.tax_calculator.exceptions;
import com.volvo.tax_calculator.dto.ResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TaxCalculationException.class)
    public ResponseEntity<ResponseDto<Void>> handleTaxCalculationException(TaxCalculationException ex) {
        log.error("Tax calculation error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDto<>(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), null));
    }

    @ExceptionHandler(VehicleDataMissingException.class)
    public ResponseEntity<ResponseDto<Void>> handleVehicleDataMissingException(VehicleDataMissingException ex) {
        log.error("Vehicle data missing: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseDto<>(HttpStatus.BAD_REQUEST, ex.getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto<Void>> handleGeneralException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDto<>(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occurred", null));
    }
}
