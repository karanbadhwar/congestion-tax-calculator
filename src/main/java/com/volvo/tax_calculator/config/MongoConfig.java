package com.volvo.tax_calculator.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing //TODO why is this required
public class MongoConfig {
}
