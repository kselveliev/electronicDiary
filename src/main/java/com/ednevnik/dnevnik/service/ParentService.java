package com.ednevnik.dnevnik.service;

import com.ednevnik.dnevnik.model.Parent;
import com.ednevnik.dnevnik.model.Student;

import java.util.Set;

public interface ParentService {
    Parent findById(Long id);
    Set<Student> getChildren(Long parentId);
    Parent addChild(Long parentId, Long studentId);
    Parent removeChild(Long parentId, Long studentId);
} 