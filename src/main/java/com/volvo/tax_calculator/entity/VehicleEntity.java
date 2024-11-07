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
    private ObjectId id;

    @NonNull
    private String vehicleType;

    @NonNull
    @Indexed(unique = true)
    private String vehicleNumber;

    @DBRef
    private List<TaxEntity> taxList = new ArrayList<>();

}
