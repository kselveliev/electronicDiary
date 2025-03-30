package com.ednevnik.dnevnik.controller;

import com.ednevnik.dnevnik.model.School;
import com.ednevnik.dnevnik.service.SchoolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/schools")
@RequiredArgsConstructor
public class SchoolController {

    private final SchoolService schoolService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<School> createSchool(@Valid @RequestBody SchoolRequest request) {
        School school = new School();
        school.setName(request.name());
        school.setAddress(request.address());
        return ResponseEntity.ok(schoolService.save(school));
    }

    @GetMapping
    public ResponseEntity<List<School>> getAllSchools() {
        return ResponseEntity.ok(schoolService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<School> getSchool(@PathVariable Long id) {
        return ResponseEntity.ok(schoolService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<School> updateSchool(@PathVariable Long id, @Valid @RequestBody SchoolRequest request) {
        School school = schoolService.findById(id);
        school.setName(request.name());
        school.setAddress(request.address());
        return ResponseEntity.ok(schoolService.save(school));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteSchool(@PathVariable Long id) {
        schoolService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}

record SchoolRequest(String name, String address) {} 