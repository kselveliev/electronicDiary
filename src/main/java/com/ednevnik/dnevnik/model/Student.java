package com.ednevnik.dnevnik.model;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import java.util.Set;

@Getter
@Entity
@DiscriminatorValue("STUDENT")
public class Student extends User {
    @ManyToOne
    @JoinColumn(name = "class_id")
    private Class studentClass;

    @ManyToMany(mappedBy = "children")
    private Set<Parent> parents;

    public void setStudentClass(Class studentClass) {
        this.studentClass = studentClass;
    }

    public void setParents(Set<Parent> parents) {
        this.parents = parents;
    }
}
