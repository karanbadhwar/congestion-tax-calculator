package com.volvo.tax_calculator.service;

import com.volvo.tax_calculator.entity.TaxEntity;
import com.volvo.tax_calculator.entity.VehicleEntity;
import com.volvo.tax_calculator.repository.TaxRepository;
import com.volvo.tax_calculator.repository.VehicleRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class taxCalculatorService {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private TaxRepository taxRepository;

    @Value("${holidays}")  // Load the holidays from application.properties
    private String holidays;

    public boolean isInNonTaxablePeriod() {
        LocalDateTime dateTime = LocalDateTime.now();
        String currentDate = dateTime.toLocalDate().toString();
        //checking if the current month is July and weekends
        Month currentMonth = dateTime.getMonth();
        //Checking for the Month of July(
        boolean isJuly = (currentMonth == Month.JULY);

        //Checking if today is a national holiday
        List<String> holidayList = Arrays.asList(holidays.split(","));
        boolean isHoliday = holidayList.contains(currentDate);

        //Checking if current day is a Weekend or not
        DayOfWeek currentDay = dateTime.getDayOfWeek();
        boolean isWeekend = (currentDay == DayOfWeek.SATURDAY || currentDay == DayOfWeek.SUNDAY);

        log.debug("Current month: {}, Current day: {}, isJuly: {}, isWeekend: {}", currentMonth, currentDay, isJuly, isWeekend);
        return isJuly || isWeekend || isHoliday;
    }

    @Transactional
    public boolean updatingTax(int currentTax, VehicleEntity vehicle) {
        try {
            LocalDateTime currentTimeDate = LocalDateTime.now();
            TaxEntity tax = TaxEntity.builder().totalTaxOwe(currentTax).taxLimit(60).transactionDate(currentTimeDate).updatedAt(currentTimeDate).build();
            TaxEntity savedTax = taxRepository.save(tax);
            VehicleEntity currentVehicle = vehicleRepository.findByVehicleNumber(vehicle.getVehicleNumber()).get();
            List<TaxEntity> taxList = currentVehicle.getTaxList();
            taxList.add(savedTax);
            currentVehicle.setTaxList(taxList);
            vehicleRepository.save(currentVehicle);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    public ResponseEntity<Integer> calculateTaxForSameDay(TaxEntity lastRecentTax, int currentHourTax, LocalDateTime currentTime, VehicleEntity vehicle) {
        int totalAmountDueForToday = lastRecentTax.getTotalTaxOwe();

        // Case when the tax limit is already reached
        if (totalAmountDueForToday == lastRecentTax.getTaxLimit()) {
            return new ResponseEntity<>(60, HttpStatus.OK);
        }

        // Case when the last tax update was within the past hour
        if (lastRecentTax.getUpdatedAt().isAfter(currentTime.minusHours(1))) {
            // Only update the tax if the current hour's tax is greater than the previous tax
            if (lastRecentTax.getTotalTaxOwe() < currentHourTax) {
                this.updatingTax(currentHourTax, vehicle);
                return new ResponseEntity<>(currentHourTax, HttpStatus.OK);
            }
        } else {
            // Case when the last tax update was older than an hour, or no tax was updated today yet
            int newTotal = totalAmountDueForToday + currentHourTax;

            // If adding the current tax exceeds the limit, cap it at the tax limit
            if (newTotal >= lastRecentTax.getTaxLimit()) {
                lastRecentTax.setTotalTaxOwe(lastRecentTax.getTaxLimit());
                lastRecentTax.setUpdatedAt(currentTime);
                taxRepository.save(lastRecentTax);
                return new ResponseEntity<>(lastRecentTax.getTaxLimit(), HttpStatus.OK);
            } else {
                // Update the tax to the new total
                this.updatingTax(newTotal, vehicle);
                return new ResponseEntity<>(newTotal, HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(totalAmountDueForToday, HttpStatus.OK); // Fallback, in case all conditions fail
    }
}
