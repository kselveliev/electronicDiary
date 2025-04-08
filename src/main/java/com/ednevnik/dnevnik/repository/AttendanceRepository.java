package com.ednevnik.dnevnik.repository;

import com.ednevnik.dnevnik.model.Attendance;
import com.ednevnik.dnevnik.model.Student;
import com.ednevnik.dnevnik.model.Subject;
import com.ednevnik.dnevnik.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByStudent(Student student);
    List<Attendance> findByTeacher(Teacher teacher);
    List<Attendance> findBySubject(Subject subject);
    List<Attendance> findByStudentClassId(Long classId);
    List<Attendance> findByCreatedTimestampBetween(Long start, Long end);
    
    // New methods for student and parent views
    List<Attendance> findByStudentOrderByCreatedTimestampDesc(Student student);
    List<Attendance> findByStudentInOrderByCreatedTimestampDesc(List<Student> students);
    List<Attendance> findByStudentClassIdAndSubjectIdOrderByCreatedTimestampDesc(Long classId, Long subjectId);

    @Query("SELECT a FROM Attendance a " +
           "WHERE a.studentClass.school.id = :schoolId AND a.subject.id = :subjectId")
    List<Attendance> findBySchoolAndSubject(@Param("schoolId") Long schoolId, @Param("subjectId") Long subjectId);
} 