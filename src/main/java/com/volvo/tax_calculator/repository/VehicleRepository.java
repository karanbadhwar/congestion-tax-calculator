package com.volvo.tax_calculator.repository;

import com.volvo.tax_calculator.entities.VehicleEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface VehicleRepository extends MongoRepository<VehicleEntity, ObjectId> {

    Optional<VehicleEntity> findByVehicleNumber(String plateNumber);
}
