package com.volvo.tax_calculator.repository;

import com.volvo.tax_calculator.entity.TaxEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TaxRepository extends MongoRepository<TaxEntity, ObjectId> {
}
