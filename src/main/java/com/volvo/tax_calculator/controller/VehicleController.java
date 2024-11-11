package com.volvo.tax_calculator.controller;

import com.volvo.tax_calculator.entities.VehicleEntity;
import com.volvo.tax_calculator.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("api/vehicle")
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;
    @PostMapping
    public void createVehicle(@RequestBody VehicleEntity vehicle) {
            vehicleService.saveVehicle(vehicle);
    }
}
