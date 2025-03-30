package com.ednevnik.dnevnik.model;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("DIRECTOR")
public class Director extends User {
    // Director-specific fields can be added here if needed
} 