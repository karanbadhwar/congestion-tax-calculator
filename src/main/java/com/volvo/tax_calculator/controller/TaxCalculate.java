package com.volvo.tax_calculator.controller;

import com.volvo.tax_calculator.entity.TaxEntity;
import com.volvo.tax_calculator.entity.VehicleEntity;
import com.volvo.tax_calculator.repository.TaxRepository;
import com.volvo.tax_calculator.service.VehicleService;
import com.volvo.tax_calculator.service.taxCalculatorService;
import com.volvo.tax_calculator.utils.CongestionTaxCalculator;
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
public class TaxCalculate {

    //TODO use AllArgsConstructor
    @Autowired
    private taxCalculatorService taxCalculatorService;

    @Autowired
    private CongestionTaxCalculator congestionTaxCalculator;

    @Autowired
    private TaxRepository taxRepository;

    @Autowired
    private VehicleService vehicleService;

    @GetMapping("/health")
    public String healthCheck() {
        return "OK";
    }

    @PostMapping
    public ResponseEntity<Integer> taxCalculation(@RequestBody VehicleEntity vehicle) {
        //TODO where are the validations before getting the vehicleType?
        String vehicleType = vehicle.getVehicleType();
        //Checking for the exempted list of vehicles
        if(isExempted(vehicleType)) {
            //TODO: This is repeated twice, you should not repeat the code
            //Why are we returning integer, should we not return the object so we can send more things in the future?
            //How do you want to send the errors to the user?
            return new ResponseEntity<>(0, HttpStatus.OK);
        }

        //Checking for the exempted days and Month(July)
        if(taxCalculatorService.isInNonTaxablePeriod()) {
            return new ResponseEntity<>(0,HttpStatus.OK);
        }

        //Checking if the Vehicle is already in the records or not
        //TODO why do you need vehicle entity?
        VehicleEntity currentVehicle = vehicleService.getVehicleInfo(vehicle.getVehicleNumber(), vehicle.getVehicleType());
//        int currentHourTax = taxCalculatorService.taxCalculationCurrentHour();
        //TODO: If it's just calculating hour tax why is the function named as calculateTax
        int currentHourTax = congestionTaxCalculator.calculateTax(LocalDateTime.now());
        //TODO: can we use this if check so we don't need to call the calculate tax?
        if(currentVehicle.getTaxList().isEmpty()) {
            taxCalculatorService.updatingTax(currentHourTax, vehicle);
            return new ResponseEntity<>(currentHourTax, HttpStatus.OK);
        } else {
            List<TaxEntity> taxList = currentVehicle.getTaxList();
            TaxEntity lastRecentTax = taxList.getLast();
            LocalDateTime currentTime = LocalDateTime.now();
            //Checking if the last tax charged was day before
            if(!lastRecentTax.getUpdatedAt().toLocalDate().isBefore(LocalDate.now())) {
                return taxCalculatorService.calculateTaxForSameDay(lastRecentTax, currentHourTax, currentTime, vehicle);
            }else {
                taxCalculatorService.updatingTax(currentHourTax,vehicle);
            }
            return new ResponseEntity<>(currentHourTax, HttpStatus.OK);
        }
    }

}
