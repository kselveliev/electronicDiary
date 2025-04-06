package com.ednevnik.dnevnik.repository;

import com.ednevnik.dnevnik.model.Class;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassRepository extends JpaRepository<Class, Long> {
} 