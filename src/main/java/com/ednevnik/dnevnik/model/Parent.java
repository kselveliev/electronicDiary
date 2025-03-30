package com.ednevnik.dnevnik.model;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Set;

@Getter
@Entity
@DiscriminatorValue("parent")
public class Parent extends User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;

    @ManyToMany
    private Set<Student> children;


    @Column(updatable = false)
    private Long createdTimestamp;
}