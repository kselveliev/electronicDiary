package com.ednevnik.dnevnik.model;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Entity
@DiscriminatorValue("student")  // Discriminator value for Student
public class Student extends User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String grade; // Grade or class

    @ManyToOne
    private School school;

    @ManyToOne
    private Class studentClass;

    @Column(updatable = false)
    private Long createdTimestamp;
}
