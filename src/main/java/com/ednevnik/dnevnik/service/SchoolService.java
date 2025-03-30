package com.ednevnik.dnevnik.service;

import com.ednevnik.dnevnik.model.School;

import java.util.List;

public interface SchoolService {
    School save(School school);
    School findById(Long id);
    List<School> findAll();
    void deleteById(Long id);
} 