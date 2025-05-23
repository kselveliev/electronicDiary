package com.ednevnik.dnevnik.init;

import com.ednevnik.dnevnik.repository.SchoolRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1) // Run before UserInitializer
@AllArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SchoolRepository schoolRepository;

    @Override
    public void run(String... args) throws Exception {
        // Initialize default data if needed
        // For example: default schools, subjects, etc.
        if (schoolRepository.count() == 0) {
            // Initialize default school(s) if needed
        }
    }
}
