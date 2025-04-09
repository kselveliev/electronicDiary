package com.ednevnik.dnevnik.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.Set;
import lombok.Setter;

@Getter
@Setter
@Entity
@DiscriminatorValue("parent")
public class Parent extends User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "parent_children",
        joinColumns = @JoinColumn(name = "parent_id"),
        inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private Set<Student> children;
}