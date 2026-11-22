package com.goldwatch.goldtracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class GoldTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GoldTrackerApplication.class, args);
    }
}
