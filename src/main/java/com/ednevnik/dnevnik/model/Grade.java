package com.ednevnik.dnevnik.model;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "grades")
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer grade; // The grade/mark given

    @ManyToOne
    private Student student;

    @ManyToOne
    private Subject subject;

    @ManyToOne
    private Teacher teacher;


    @Column(updatable = false)
    private Long createdTimestamp;

}

