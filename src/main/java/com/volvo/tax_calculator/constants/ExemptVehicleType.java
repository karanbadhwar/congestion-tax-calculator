package com.volvo.tax_calculator.constants;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum ExemptVehicleType  {
    EMERGENCY,
    BUS,
    DIPLOMAT,
    MOTORCYCLE,
    MILITARY,
    FOREIGN;

    public static boolean isExempted(String vehicleType) {
        //TODO What happens if vehicleType is null
        try {
          ExemptVehicleType type = ExemptVehicleType.valueOf(vehicleType.toUpperCase());
          return true;
        } catch(IllegalArgumentException e) {
//            log.info(e.getMessage());
            return false;
        }
    }
}
