package com.volvo.tax_calculator.controller;

import com.volvo.tax_calculator.entity.VehicleEntity;
import com.volvo.tax_calculator.repository.VehicleRepository;
import com.volvo.tax_calculator.service.VehicleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("api/vehicle")
//TODO: Why does a service which takes care of tax calculation taking care of storing vehicles as well?
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;
    @PostMapping
    public void createVehicle(@RequestBody VehicleEntity vehicle) {
            vehicleService.saveVehicle(vehicle);
    }
}
