package com.volvo.tax_calculator.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tax")
public class TaxEntity {

    @Id
    private ObjectId id;
    private int totalTaxOwe;
    @Builder.Default
    private int taxLimit = 60;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    private LocalDateTime transactionDate;
}
