package com.volvo.tax_calculator.utils;
import com.google.common.collect.ImmutableRangeMap;
import com.google.common.collect.Range;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Slf4j
@Component
public class CongestionTaxCalculator {

    private final ImmutableRangeMap<LocalTime, Integer> taxBrackets;

    public CongestionTaxCalculator() {
        this.taxBrackets = ImmutableRangeMap.<LocalTime, Integer>builder()
                .put(Range.closedOpen(createTime(6, 0), createTime(6, 30)), 8)
                .put(Range.closedOpen(createTime(6, 30), createTime(7, 0)), 13)
                .put(Range.closedOpen(createTime(7, 0), createTime(8, 0)), 18)
                .put(Range.closedOpen(createTime(8, 0), createTime(8, 30)), 13)
                .put(Range.closedOpen(createTime(8, 30), createTime(15, 0)), 8)
                .put(Range.closedOpen(createTime(15, 0), createTime(15, 30)), 13)
                .put(Range.closedOpen(createTime(15, 30), createTime(17, 0)), 18)
                .put(Range.closedOpen(createTime(17, 0), createTime(18, 0)), 13)
                .put(Range.closedOpen(createTime(18, 0), createTime(18, 30)), 8)
                .put(Range.closedOpen(createTime(18, 30), createTime(23, 59)), 0)
                .put(Range.closedOpen(createTime(0, 0), createTime(6, 0)), 0) // midnight to 6:00 AM
                .build();
    }

    private LocalTime createTime(int hour, int minute) {
        return LocalTime.of(hour, minute);
    }


    // Main method to calculate tax
    public int calculateTax(LocalDateTime dateTime) {
        log.info("Calculating congestion tax for date/time: {}", dateTime);

        //if had int, not possible to compare with != null, that is why used Integer wrapper class
        Integer taxAmount = taxBrackets.get(LocalTime.from(dateTime));

        if (taxAmount != null) {
            return taxAmount;
        }

        log.debug("No matching tax bracket found. Returning default tax amount.");
        return 0;
    }

}
