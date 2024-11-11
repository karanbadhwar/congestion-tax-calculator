package com.volvo.tax_calculator.service;

import com.volvo.tax_calculator.entities.TaxEntity;
import com.volvo.tax_calculator.entities.VehicleEntity;
import com.volvo.tax_calculator.repository.TaxRepository;
import com.volvo.tax_calculator.repository.VehicleRepository;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;

import java.time.LocalDateTime;
import java.time.Month;

import java.util.*;

@Slf4j
@Service
public class TaxCalculatorService {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private TaxRepository taxRepository;

    @Value("${holidays}")  // Load the holidays from application.properties
    private String holidays;

    private Set<String> holidaySet;

    @PostConstruct
    public void init() {
        holidaySet = new HashSet<>(Arrays.asList(holidays.split(",")));
    }

    public boolean isInNonTaxablePeriod() {
        LocalDateTime dateTime = LocalDateTime.now();
        String currentDate = dateTime.toLocalDate().toString();

        boolean isJuly = dateTime.getMonth() == Month.JULY;
        boolean isHoliday = holidaySet.contains(currentDate);
        boolean isWeekend = dateTime.getDayOfWeek() == DayOfWeek.SATURDAY || dateTime.getDayOfWeek() == DayOfWeek.SUNDAY;

        log.debug("Current month: {}, Current day: {}, isJuly: {}, isWeekend: {}", dateTime.getMonth(), dateTime.getDayOfWeek(), isJuly, isWeekend);
        return isJuly || isWeekend || isHoliday;
    }

    @Transactional
    public boolean updatingTax(int currentTax, VehicleEntity vehicle, LocalDateTime currentTimeDate) {
        try {
            TaxEntity tax = TaxEntity.builder().totalTaxOwe(currentTax).taxLimit(60).transactionDate(currentTimeDate).updatedAt(currentTimeDate).build();
            TaxEntity savedTax = taxRepository.save(tax);
            VehicleEntity currentVehicle = vehicleRepository.findByVehicleNumber(vehicle.getVehicleNumber())
                    .orElseThrow(() -> new EntityNotFoundException("Vehicle not found"));
            currentVehicle.getTaxList().add(savedTax);
            vehicleRepository.save(currentVehicle);
            return true;
        } catch (EntityNotFoundException e) {
            return false;
        } catch (Exception e) {
            log.error("Error updating tax: {}", e.getMessage());
            return false;
        }
    }

    public int calculateTaxForSameDay(TaxEntity lastRecentTax, int currentHourTax, LocalDateTime currentTime, VehicleEntity vehicle) {
        int totalAmountDueForToday = lastRecentTax.getTotalTaxOwe();

        // If the daily tax limit is already reached, return the limit
        if (totalAmountDueForToday >= lastRecentTax.getTaxLimit()) {
            return lastRecentTax.getTaxLimit();
        }

        // If last tax update was within the past hour
        if (lastRecentTax.getUpdatedAt().isAfter(currentTime.minusHours(1))) {
            // Update with the higher tax if current tax is greater
            return handleTaxUpdateForSameHour(lastRecentTax, currentHourTax, vehicle);
        } else if (lastRecentTax.getUpdatedAt().toLocalDate().equals(currentTime.toLocalDate())) {
            // If it's the same day but a different hour, add the tax up to the limit for the day
            return handleTaxUpdateForDifferentHour(lastRecentTax, currentHourTax, vehicle, currentTime);
        } else {
            // If it's a new day, create a new tax entry
            return handleTaxUpdateForNewDay(currentHourTax, vehicle, currentTime);
        }
    }

    private int handleTaxUpdateForSameHour(TaxEntity lastRecentTax, int currentHourTax, VehicleEntity vehicle) {
        // If currentHourTax is greater than the recorded tax for the last hour, replace it
        if (currentHourTax > lastRecentTax.getTotalTaxOwe()) {
            lastRecentTax.setTotalTaxOwe(currentHourTax);
            taxRepository.save(lastRecentTax);
            log.info("Updated tax for the same hour with a higher value: {}", currentHourTax);
        }
        return lastRecentTax.getTotalTaxOwe();
    }

    private int handleTaxUpdateForDifferentHour(TaxEntity lastRecentTax, int currentHourTax, VehicleEntity vehicle, LocalDateTime currentTime) {
        int newTotal = lastRecentTax.getTotalTaxOwe() + currentHourTax;

        // If adding the current tax exceeds the daily limit, cap it at the limit
        if (newTotal > lastRecentTax.getTaxLimit()) {
            newTotal = lastRecentTax.getTaxLimit();
        }

        lastRecentTax.setTotalTaxOwe(newTotal);
        lastRecentTax.setUpdatedAt(currentTime);
        taxRepository.save(lastRecentTax);
        log.info("Added tax for a different hour on the same day: {}", currentHourTax);
        return newTotal;
    }

    private int handleTaxUpdateForNewDay(int currentHourTax, VehicleEntity vehicle, LocalDateTime currentTime) {
        boolean success = updatingTax(currentHourTax, vehicle, currentTime);
        if (!success) {
            log.warn("Failed to create new tax entry for a new day.");
            return -1;  // Indicate failure with a specific value
        }
        log.info("Created new tax entry for a new day: {}", currentHourTax);
        return currentHourTax;
    }
}
