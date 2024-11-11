package com.volvo.tax_calculator.controller;

import com.volvo.tax_calculator.dto.ResponseDto;
import com.volvo.tax_calculator.entities.TaxEntity;
import com.volvo.tax_calculator.entities.VehicleEntity;
import com.volvo.tax_calculator.repository.TaxRepository;
import com.volvo.tax_calculator.service.TaxCalculatorService;
import com.volvo.tax_calculator.service.VehicleService;
import com.volvo.tax_calculator.utils.CongestionTaxCalculator;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.List;

import static com.volvo.tax_calculator.constants.ExemptVehicleType.isExempted;

@RestController
@RequestMapping("api/tax-calculator")
@AllArgsConstructor
public class TaxCalculate {

    private final TaxCalculatorService taxCalculatorService;
    private final CongestionTaxCalculator congestionTaxCalculator;
    private final VehicleService vehicleService;

    @GetMapping("/health")
    public String healthCheck() {
        return "OK";
    }

    @PostMapping
    public ResponseEntity<ResponseDto<Integer>> taxCalculation(@RequestBody VehicleEntity vehicle) {
        try {
            ResponseDto<Integer> response = calculateTaxForVehicle(vehicle);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseDto<>(HttpStatus.INTERNAL_SERVER_ERROR, "Error calculating tax", null));
        }
    }

    public ResponseDto<Integer> calculateTaxForVehicle(VehicleEntity vehicle) {
        String vehicleType = vehicle.getVehicleType();

        if (isExempted(vehicleType)) {
            return new ResponseDto<>(HttpStatus.OK, "Vehicle is exempted from tax", 0);
        }

        if (taxCalculatorService.isInNonTaxablePeriod()) {
            return new ResponseDto<>(HttpStatus.OK, "Currently in Non-taxable period", 0);
        }

        VehicleEntity currentVehicle = vehicleService.getVehicleInfo(vehicle.getVehicleNumber(), vehicle.getVehicleType());
        int currentHourTax = congestionTaxCalculator.calculateTax(LocalDateTime.now());

        if (currentVehicle.getTaxList().isEmpty()) {
            boolean isTaxUpdated = taxCalculatorService.updatingTax(currentHourTax, vehicle, LocalDateTime.now());
            if (isTaxUpdated) {
                return new ResponseDto<>(HttpStatus.OK, "Initial tax entry created", currentHourTax);
            } else {
                return new ResponseDto<>(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create tax entry", null);
            }
        } else {
            // Handle tax update based on previous tax entry
            return calculateOrUpdateTaxForExistingRecord(currentVehicle, currentHourTax);

        }
    }

    private ResponseDto<Integer> calculateOrUpdateTaxForExistingRecord(VehicleEntity currentVehicle, int currentHourTax) {
        List<TaxEntity> taxList = currentVehicle.getTaxList();
        TaxEntity lastRecentTax = taxList.getLast();
        LocalDateTime currentTime = LocalDateTime.now();

        //Checking if the last tax charged was day before
        if (lastRecentTax.getUpdatedAt().toLocalDate().isBefore(LocalDate.now())) {
            boolean isTaxUpdated = taxCalculatorService.updatingTax(currentHourTax, currentVehicle, currentTime);
            return isTaxUpdated
                    ? new ResponseDto<>(HttpStatus.OK, "New day tax entry created", currentHourTax)
                    : new ResponseDto<>(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create new tax entry", null);
        }

        // Calculate tax for the same day
        int updatedTax = taxCalculatorService.calculateTaxForSameDay(lastRecentTax, currentHourTax, currentTime, currentVehicle);
        if (updatedTax == -1) {
            return new ResponseDto<>(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update tax for the same day", null);
        }

        return new ResponseDto<>(HttpStatus.OK, "Tax updated for the same day", updatedTax);
    }
}


