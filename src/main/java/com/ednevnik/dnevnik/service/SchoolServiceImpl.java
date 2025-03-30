package com.ednevnik.dnevnik.service;

import com.ednevnik.dnevnik.model.School;
import com.ednevnik.dnevnik.repository.SchoolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SchoolServiceImpl implements SchoolService {

    private final SchoolRepository schoolRepository;

    @Override
    public School save(School school) {
        return schoolRepository.save(school);
    }

    @Override
    public School findById(Long id) {
        return schoolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("School not found"));
    }

    @Override
    public List<School> findAll() {
        return schoolRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        schoolRepository.deleteById(id);
    }
} 