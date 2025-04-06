package com.ednevnik.dnevnik.service;

import com.ednevnik.dnevnik.model.Parent;
import com.ednevnik.dnevnik.model.Student;
import com.ednevnik.dnevnik.repository.ParentRepository;
import com.ednevnik.dnevnik.repository.StudentRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@AllArgsConstructor
@Service
@Transactional
public class ParentServiceImpl implements ParentService {

    private final ParentRepository parentRepository;
    private final StudentRepository studentRepository;

    @Override
    public Set<Student> findChildrenByParentId(Long parentId) {
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("Parent not found"));
        // Force initialization of the children collection
        parent.getChildren().size();
        return parent.getChildren();
    }

    @Override
    public void assignChildToParent(Long studentId, Long parentId) {
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("Parent not found"));
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        parent.getChildren().add(student);
        parentRepository.save(parent);
    }

    @Override
    public void removeChildFromParent(Long studentId, Long parentId) {
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("Parent not found"));
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        parent.getChildren().remove(student);
        parentRepository.save(parent);
    }
} 