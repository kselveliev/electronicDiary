package com.ednevnik.dnevnik.repository;

import com.ednevnik.dnevnik.model.Parent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParentRepository extends JpaRepository<Parent, Long> {
} 