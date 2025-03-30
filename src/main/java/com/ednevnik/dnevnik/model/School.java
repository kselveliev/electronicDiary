package com.ednevnik.dnevnik.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "schools")
public class School {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String address;
    private String city;
    private String phoneNumber;
    private String email;

    @OneToOne
    @JoinColumn(name = "director_id")
    private User director;

    public void setDirector(User director) {
        this.director = director;
        if (director != null) {
            director.setSchool(this);
        }
    }
}
