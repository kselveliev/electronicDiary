package com.ednevnik.dnevnik.model;

import jakarta.persistence.*;


@Entity
@Table(name = "attendance")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Boolean present;

    @ManyToOne
    private Student student;

    @ManyToOne
    private Teacher teacher;

    @Column(updatable = false)
    private Long createdTimestamp;

}
