package com.ednevnik.dnevnik.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@Entity
@Table(name = "classes")
public class Class {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer grade;

    @ManyToOne
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne
    @JoinColumn(name = "class_teacher_id", nullable = false)
    private Teacher classTeacher;

    @OneToMany(mappedBy = "studentClass")
    private Set<Student> students;

    @Column(updatable = false)
    private Long createdTimestamp;
}
