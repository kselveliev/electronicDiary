package com.ednevnik.dnevnik.model;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;


@Getter
@Entity
@DiscriminatorValue("teacher")
public class Teacher extends User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;

    @Column(updatable = false)
    private Long createdTimestamp;
}
