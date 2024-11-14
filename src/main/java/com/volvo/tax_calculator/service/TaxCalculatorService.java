package com.volvo.tax_calculator.service;
import com.mongodb.MongoWriteException;
import com.volvo.tax_calculator.entities.TaxEntity;
import com.volvo.tax_calculator.repository.TaxRepository;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
public class TaxCalculatorService {

    private final TaxRepository taxRepository;
    private final Environment environment;

    private Set<String> holidaySet;

    @PostConstruct
    public void init() {
        String holidays = environment.getProperty("holidays");
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

    public TaxEntity getLatestTax(String vehicleNumber) {
        List<TaxEntity> taxList = taxRepository.findByVehicleNumberOrderByUpdatedAtDesc(vehicleNumber);

        if(taxList.isEmpty()) {
            return null;
        }

        return taxList.getFirst();
    }

    public boolean updatingTax(int currentTax, LocalDateTime currentTimeDate, String vehicleNumber) {
        try {
            TaxEntity tax = TaxEntity.builder()
                    .totalTaxOwe(currentTax)
                    .taxLimit(60)
                    .vehicleNumber(vehicleNumber)
                    .updatedAt(currentTimeDate)
                    .build();

            taxRepository.save(tax);
            return true;
        } catch (DataIntegrityViolationException e) {
            log.error("Constraint Error while updating tax: {}", e.getMessage());
            return false;
        } catch(MongoWriteException e) {
            log.error("MongoDB write error while updating tax: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Error updating tax: {}", e.getMessage());
            return false;
        }
    }

    public int calculateTaxForSameDay(TaxEntity lastRecentTax, int currentHourTax, LocalDateTime currentTime) {
        int totalAmountDueForToday = lastRecentTax.getTotalTaxOwe();

        if (totalAmountDueForToday >= lastRecentTax.getTaxLimit()) {
            return lastRecentTax.getTaxLimit();
        }

        if (lastRecentTax.getUpdatedAt().isAfter(currentTime.minusHours(1))) {
            return handleTaxUpdateForSameHour(lastRecentTax, currentHourTax);
        } else if (lastRecentTax.getUpdatedAt().toLocalDate().equals(currentTime.toLocalDate())) {
            return handleTaxUpdateForDifferentHour(lastRecentTax, currentHourTax, currentTime);
        } else {
            return handleTaxUpdateForNewDay(currentHourTax, currentTime, lastRecentTax.getVehicleNumber());
        }
    }

    private int handleTaxUpdateForSameHour(TaxEntity lastRecentTax, int currentHourTax) {
        if (currentHourTax > lastRecentTax.getTotalTaxOwe()) {
            savingTax(lastRecentTax, currentHourTax);
            log.info("Updated tax for the same hour with a higher value: {}", currentHourTax);
        }
        return lastRecentTax.getTotalTaxOwe();
    }

    private int handleTaxUpdateForDifferentHour(TaxEntity lastRecentTax, int currentHourTax, LocalDateTime currentTime) {
        int newTotal = lastRecentTax.getTotalTaxOwe() + currentHourTax;

        if (newTotal > lastRecentTax.getTaxLimit()) {
            newTotal = lastRecentTax.getTaxLimit();
        }

        lastRecentTax.setUpdatedAt(currentTime);
        savingTax(lastRecentTax,newTotal);
        log.info("Added tax for a different hour on the same day: {}", currentHourTax);
        return newTotal;
    }

    private int handleTaxUpdateForNewDay(int currentHourTax, LocalDateTime currentTime, String vehicleNumber) {
        boolean success = updatingTax(currentHourTax, currentTime, vehicleNumber);
        if (!success) {
            log.warn("Failed to create new tax entry for a new day.");
            return -1;  // Indicate failure with a specific value
        }
        log.info("Created new tax entry for a new day: {}", currentHourTax);
        return currentHourTax;
    }

    private void savingTax(TaxEntity taxToUpdate, int amount) {
        taxToUpdate.setTotalTaxOwe(amount);
        taxRepository.save(taxToUpdate);
    }

}
