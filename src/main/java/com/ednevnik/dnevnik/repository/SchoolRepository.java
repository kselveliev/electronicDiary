package com.ednevnik.dnevnik.repository;

import com.ednevnik.dnevnik.model.School;
import com.ednevnik.dnevnik.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SchoolRepository extends JpaRepository<School, Long> {
    Optional<School> findByDirector(User director);
} 