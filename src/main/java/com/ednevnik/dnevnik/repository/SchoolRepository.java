package com.ednevnik.dnevnik.repository;

import com.ednevnik.dnevnik.model.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchoolRepository extends JpaRepository<School, Long> {
} 