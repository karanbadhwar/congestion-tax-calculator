package com.volvo.tax_calculator.utils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class CongestionTaxCalculator {

    // Enum for Tax Brackets
    private enum TaxBracket {
        //TODO this is bad, like really really bad. Use rangeMap instead or LinkedHashMap with startTime and endTime
        MORNING_06_00_TO_06_29(6, 0, 6, 29, 8),
        MORNING_06_30_TO_06_59(6, 30, 6, 59, 13),
        MORNING_07_00_TO_07_59(7, 0, 7, 59, 18),
        MORNING_08_00_TO_08_29(8, 0, 8, 29, 13),
        DAYTIME_08_30_TO_14_59(8, 30, 14, 59, 8),
        AFTERNOON_15_00_TO_15_29(15, 0, 15, 29, 13),
        AFTERNOON_15_30_TO_16_59(15, 30, 16, 59, 18),
        EVENING_17_00_TO_17_59(17, 0, 17, 59, 13),
        EVENING_18_00_TO_18_29(18, 0, 18, 29, 8),
        NIGHT_18_30_TO_05_59(18, 30, 5, 59, 0); // Note: 5:59 is actually the next day

        private final int startHour;
        private final int startMinute;
        private final int endHour;
        private final int endMinute;
        @Getter
        private final int taxAmount;

        TaxBracket(int startHour, int startMinute, int endHour, int endMinute, int taxAmount) {
            this.startHour = startHour;
            this.startMinute = startMinute;
            this.endHour = endHour;
            this.endMinute = endMinute;
            this.taxAmount = taxAmount;
        }

        // Check if current time is in the tax bracket range
        public boolean isInRange(LocalDateTime dateTime) {
            return isBetween(dateTime.getHour(), dateTime.getMinute(), startHour, startMinute, endHour, endMinute);
        }

        private static boolean isBetween(int hour, int minute, int startHour, int startMinute, int endHour, int endMinute) {
            //TODO the year was mentioned in the question as a sample, the year can change everytime
            LocalDateTime current = LocalDateTime.of(2013, 1, 1, hour, minute);
            LocalDateTime start = LocalDateTime.of(2013, 1, 1, startHour, startMinute);
            LocalDateTime end = LocalDateTime.of(2013, 1, 1, endHour, endMinute);
            return !current.isBefore(start) && !current.isAfter(end);
        }
    }

    // Main method to calculate tax
    public int calculateTax(LocalDateTime dateTime) {
        log.info("Calculating congestion tax for date/time: {}", dateTime);

        // Iterate through all tax brackets and check if the date/time falls within one of the brackets
        //TODO: why did you even create the enum if you're going to use it as a list?
        //Can you do this operation in O(1)
        for (TaxBracket bracket : TaxBracket.values()) {
            if (bracket.isInRange(dateTime)) {
                log.debug("Tax bracket matched: {}", bracket);
                return bracket.getTaxAmount();
            }
        }

        log.debug("No matching tax bracket found. Returning default tax amount.");
        return 0;
    }

}
