package com.volvo.tax_calculator.controller;
import com.volvo.tax_calculator.dto.ResponseDto;
import com.volvo.tax_calculator.entities.TaxEntity;
import com.volvo.tax_calculator.entities.VehicleEntity;
import com.volvo.tax_calculator.exceptions.TaxCalculationException;
import com.volvo.tax_calculator.exceptions.VehicleDataMissingException;
import com.volvo.tax_calculator.service.TaxCalculatorService;
import com.volvo.tax_calculator.utils.CongestionTaxCalculator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static com.volvo.tax_calculator.constants.ExemptVehicleType.isExempted;

@Slf4j
@RestController
@RequestMapping("api/tax-calculator")
@AllArgsConstructor
public class TaxCalculate {

    private final TaxCalculatorService taxCalculatorService;
    private final CongestionTaxCalculator congestionTaxCalculator;

    @GetMapping("/health")
    public String healthCheck() {
        return "OK";
    }

    @PostMapping
    public ResponseEntity<ResponseDto<Integer>> taxCalculation(@RequestBody VehicleEntity vehicle) {
        try {
            // Validate required fields
            if (vehicle.vehicleType() == null || vehicle.vehicleType().isEmpty() ||
                    vehicle.vehicleNumber() == null || vehicle.vehicleNumber().isEmpty()) {
                throw new VehicleDataMissingException("Vehicle type or number is missing.");
            }
            ResponseDto<Integer> response = calculateTaxForVehicle(vehicle);
            return ResponseEntity.ok(response);
        } catch (VehicleDataMissingException | TaxCalculationException ex ) {
            throw ex;
        } catch(Exception ex) {
            log.error("Unexpected error during tax calculation", ex);
            throw new TaxCalculationException("Error calculating tax");
        }
    }

    public ResponseDto<Integer> calculateTaxForVehicle(VehicleEntity vehicle) {
        if (vehicle == null) {
            throw new VehicleDataMissingException("Vehicle data is missing");
        }

        if (isExempted(vehicle.vehicleType())) {
            return new ResponseDto<>(HttpStatus.OK, "Vehicle is exempted from tax", 0);
        }

        if (taxCalculatorService.isInNonTaxablePeriod()) {
            return new ResponseDto<>(HttpStatus.OK, "Currently in Non-taxable period", 0);
        }

        int currentHourTax = congestionTaxCalculator.calculateTax(LocalDateTime.now());

        TaxEntity lastRecentTax = taxCalculatorService.getLatestTax(vehicle.vehicleNumber());
        LocalDateTime currentTime = LocalDateTime.now();
        if(lastRecentTax != null) {
            return calculateOrUpdateTaxForExistingRecord(lastRecentTax, currentHourTax, currentTime, vehicle.vehicleNumber());
        }

        return creatingNewTax(currentHourTax, currentTime, vehicle.vehicleNumber());

    }

    private ResponseDto<Integer> calculateOrUpdateTaxForExistingRecord(TaxEntity lastRecentTax, int currentHourTax, LocalDateTime currentTime, String vehicleNumber) {

        if (lastRecentTax.getUpdatedAt().toLocalDate().isBefore(LocalDate.now())) {
           return creatingNewTax(currentHourTax, currentTime, vehicleNumber);
        }

        int updatedTax = taxCalculatorService.calculateTaxForSameDay(lastRecentTax, currentHourTax, currentTime);
        if (updatedTax == -1) {
            throw new TaxCalculationException("Failed to update tax for the same day");
        }

        return new ResponseDto<>(HttpStatus.OK, "Tax updated for the same day", updatedTax);
    }

    private ResponseDto<Integer> creatingNewTax(int currentHourTax, LocalDateTime currentTime, String vehicleNumber) {
        boolean isTaxUpdated = taxCalculatorService.updatingTax(currentHourTax, currentTime, vehicleNumber);
        if (!isTaxUpdated) {
            throw new TaxCalculationException("Failed to create new tax entry");
        }
        return new ResponseDto<>(HttpStatus.OK, "New day tax entry created", currentHourTax);
    }

}


