package com.ednevnik.dnevnik.service;

import com.ednevnik.dnevnik.model.Parent;
import com.ednevnik.dnevnik.model.Student;
import com.ednevnik.dnevnik.repository.ParentRepository;
import com.ednevnik.dnevnik.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ParentServiceImpl implements ParentService {

    private final ParentRepository parentRepository;
    private final StudentRepository studentRepository;

    @Override
    @Transactional(readOnly = true)
    public Parent findById(Long id) {
        return parentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Parent not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Student> getChildren(Long parentId) {
        Parent parent = findById(parentId);
        // Force initialization of the children collection
        parent.getChildren().size();
        return parent.getChildren();
    }

    @Override
    @Transactional
    public Parent addChild(Long parentId, Long studentId) {
        Parent parent = findById(parentId);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        parent.getChildren().add(student);
        return parentRepository.save(parent);
    }

    @Override
    @Transactional
    public Parent removeChild(Long parentId, Long studentId) {
        Parent parent = findById(parentId);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        parent.getChildren().remove(student);
        return parentRepository.save(parent);
    }
} 