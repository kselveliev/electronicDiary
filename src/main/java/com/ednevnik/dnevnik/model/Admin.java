package com.ednevnik.dnevnik.model;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("ADMIN")
public class Admin extends User {
    // Admin-specific fields can be added here if needed
} 