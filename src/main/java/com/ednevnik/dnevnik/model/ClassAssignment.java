package com.ednevnik.dnevnik.model;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
public class ClassAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne
    @JoinColumn(name = "class_id", nullable = false)
    private Class schoolClass;  // Link to the class entity


    @Column(updatable = false)
    private Long createdTimestamp;
}
