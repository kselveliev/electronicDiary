package com.ednevnik.dnevnik.model;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "subjects")
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    private Class studentClass;

    @Column(updatable = false)
    private Long createdTimestamp;
}
