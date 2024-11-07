package com.volvo.tax_calculator.service;

import com.volvo.tax_calculator.entity.VehicleEntity;
import com.volvo.tax_calculator.repository.VehicleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;

    public void saveVehicle(VehicleEntity vehicle) {
        try{
            vehicleRepository.save(vehicle);
        }catch(Exception e) {
            log.error(e.getMessage());
        }
    }

    public VehicleEntity getVehicleInfo(String plateNumber, String vehicleType) {
        Optional<VehicleEntity> vehicle = vehicleRepository.findByVehicleNumber(plateNumber);
        if(vehicle.isEmpty()) {
            VehicleEntity v1 = VehicleEntity.builder().vehicleNumber(plateNumber).vehicleType(vehicleType).build();
            this.saveVehicle(v1);
            return v1;
        }
        return vehicle.get();

    }
}
