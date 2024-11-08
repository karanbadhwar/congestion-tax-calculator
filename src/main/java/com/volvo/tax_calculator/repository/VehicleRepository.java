package com.volvo.tax_calculator.repository;

import com.volvo.tax_calculator.entity.VehicleEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

//TODO: Why do you need to save the vehicle in the DB?
public interface VehicleRepository extends MongoRepository<VehicleEntity, ObjectId> {

    Optional<VehicleEntity> findByVehicleNumber(String plateNumber);
}
