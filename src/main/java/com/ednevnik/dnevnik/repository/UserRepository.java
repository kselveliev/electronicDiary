package com.ednevnik.dnevnik.repository;

import com.ednevnik.dnevnik.model.School;
import com.ednevnik.dnevnik.model.User;
import com.ednevnik.dnevnik.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByNationalId(String nationalId);
    List<User> findByRole(UserRole role);
    long countByRole(UserRole role);
    
    @Query("SELECT u FROM User u WHERE TYPE(u) IN (Teacher, Student) AND u.school = :school AND u.role = :role")
    List<User> findBySchoolAndRole(School school, UserRole role);
    
    @Query("SELECT u FROM User u WHERE TYPE(u) IN (Teacher, Student) AND u.role = :role AND u.school IS NULL")
    List<User> findByRoleAndSchoolIsNull(UserRole role);

    @Query("SELECT u FROM User u WHERE TYPE(u) = Director AND u.role = 'ROLE_DIRECTOR' AND u.school IS NULL")
    List<User> findAvailableDirectors();

    @Query("SELECT u FROM User u WHERE u.school = :school")
    List<User> findAllBySchool(School school);
}
