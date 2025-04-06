package com.ednevnik.dnevnik.service;

import com.ednevnik.dnevnik.model.Student;
import java.util.Set;

public interface ParentService {
    Set<Student> findChildrenByParentId(Long parentId);
    void assignChildToParent(Long studentId, Long parentId);
    void removeChildFromParent(Long studentId, Long parentId);
} 