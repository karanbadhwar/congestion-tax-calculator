package com.volvo.tax_calculator.entity;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "vehicle")
public class VehicleEntity {

    @Id
    //TODO what is object ID and why not use String or Integer instead of this
    private ObjectId id;

    @NonNull
    //Are you using the results of NonNull?
    private String vehicleType;

    @NonNull
    @Indexed(unique = true)
    //When inserting in the db does this need to be unique? Can a single car not have multiple instances?
    private String vehicleNumber;

    @DBRef
    private List<TaxEntity> taxList = new ArrayList<>();

}
