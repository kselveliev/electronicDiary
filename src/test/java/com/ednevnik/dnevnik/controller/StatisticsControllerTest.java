package com.ednevnik.dnevnik.controller;

import com.ednevnik.dnevnik.model.*;
import com.ednevnik.dnevnik.repository.*;
import com.ednevnik.dnevnik.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsControllerTest {

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private SchoolRepository schoolRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetailsImpl userDetails;

    @InjectMocks
    private StatisticsController statisticsController;

    private Admin admin;
    private Director director;
    private School school;
    private Subject subject;
    private List<Grade> grades;
    private List<Attendance> attendances;
    private List<School> schools;
    private List<Subject> subjects;

    @BeforeEach
    void setUp() {
        // Setup test data
        school = new School();
        school.setId(1L);
        school.setName("Test School");

        admin = new Admin();
        admin.setId(1L);
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setRole(UserRole.ROLE_ADMIN);

        director = new Director();
        director.setId(2L);
        director.setFirstName("Director");
        director.setLastName("User");
        director.setRole(UserRole.ROLE_DIRECTOR);
        director.setSchool(school);

        subject = new Subject();
        subject.setId(1L);
        subject.setName("Mathematics");

        // Create test grades
        Grade grade1 = new Grade();
        grade1.setId(1L);
        grade1.setGrade(5);
        grade1.setSubject(subject);
        grade1.setCreatedTimestamp(System.currentTimeMillis());

        Grade grade2 = new Grade();
        grade2.setId(2L);
        grade2.setGrade(4);
        grade2.setSubject(subject);
        grade2.setCreatedTimestamp(System.currentTimeMillis());

        grades = Arrays.asList(grade1, grade2);

        // Create test attendances
        Attendance attendance1 = new Attendance();
        attendance1.setId(1L);
        attendance1.setPresent(false);
        attendance1.setSubject(subject);
        attendance1.setCreatedTimestamp(System.currentTimeMillis());

        Attendance attendance2 = new Attendance();
        attendance2.setId(2L);
        attendance2.setPresent(true);
        attendance2.setSubject(subject);
        attendance2.setCreatedTimestamp(System.currentTimeMillis());

        attendances = Arrays.asList(attendance1, attendance2);

        schools = Collections.singletonList(school);
        subjects = Collections.singletonList(subject);
    }

    @Test
    void showStatisticsShouldAddAllDataForAdmin() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(admin);
        when(schoolRepository.findAll()).thenReturn(schools);
        when(subjectRepository.findAll()).thenReturn(subjects);
        when(gradeRepository.findAll()).thenReturn(grades);
        when(attendanceRepository.findAll()).thenReturn(attendances);

        // When
        String viewName = statisticsController.showStatistics(null, null, authentication, model);

        // Then
        verify(model).addAttribute("schools", schools);
        verify(model).addAttribute("isAdmin", true);
        verify(model, times(2)).addAttribute("subjects", subjects);
        verify(model).addAttribute(eq("gradeDistribution"), any());
        verify(model).addAttribute(eq("attendanceDistribution"), any());
        verify(model).addAttribute("selectedSchoolId", null);
        verify(model).addAttribute("selectedSubjectId", null);
        assertEquals("statistics/index", viewName);
    }

    @Test
    void showStatisticsShouldAddSchoolDataForDirector() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(director);
        when(subjectRepository.findAll()).thenReturn(subjects);
        when(gradeRepository.findBySchoolAndSubject(school.getId(), subject.getId())).thenReturn(grades);
        when(attendanceRepository.findBySchoolAndSubject(school.getId(), subject.getId())).thenReturn(attendances);

        // When
        String viewName = statisticsController.showStatistics(null, null, authentication, model);

        // Then
        verify(model).addAttribute("isAdmin", false);
        verify(model, times(2)).addAttribute("subjects", subjects);
        verify(model).addAttribute(eq("gradeDistribution"), any());
        verify(model).addAttribute(eq("attendanceDistribution"), any());
        verify(model).addAttribute("selectedSchoolId", null);
        verify(model).addAttribute("selectedSubjectId", null);
        assertEquals("statistics/index", viewName);
    }

    @Test
    void showStatisticsShouldFilterBySchoolForAdmin() {
        // Given
        Long schoolId = 1L;
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(admin);
        when(schoolRepository.findAll()).thenReturn(schools);
        when(subjectRepository.findAll()).thenReturn(subjects);
        when(gradeRepository.findBySchoolAndSubject(schoolId, subject.getId())).thenReturn(grades);
        when(attendanceRepository.findBySchoolAndSubject(schoolId, subject.getId())).thenReturn(attendances);

        // When
        String viewName = statisticsController.showStatistics(schoolId, null, authentication, model);

        // Then
        verify(model).addAttribute("schools", schools);
        verify(model).addAttribute("isAdmin", true);
        verify(model, times(2)).addAttribute("subjects", subjects);
        verify(model).addAttribute(eq("gradeDistribution"), any());
        verify(model).addAttribute(eq("attendanceDistribution"), any());
        verify(model).addAttribute("selectedSchoolId", schoolId);
        verify(model).addAttribute("selectedSubjectId", null);
        assertEquals("statistics/index", viewName);
    }

    @Test
    void showStatisticsShouldFilterBySubject() {
        // Given
        Long subjectId = 1L;
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(admin);
        when(schoolRepository.findAll()).thenReturn(schools);
        when(subjectRepository.findAll()).thenReturn(subjects);
        when(gradeRepository.findAll()).thenReturn(grades);
        when(attendanceRepository.findAll()).thenReturn(attendances);

        // When
        String viewName = statisticsController.showStatistics(null, subjectId, authentication, model);

        // Then
        verify(model).addAttribute("schools", schools);
        verify(model).addAttribute("isAdmin", true);
        verify(model, times(2)).addAttribute("subjects", Collections.singletonList(subject));
        verify(model).addAttribute(eq("gradeDistribution"), any());
        verify(model).addAttribute(eq("attendanceDistribution"), any());
        verify(model).addAttribute("selectedSchoolId", null);
        verify(model).addAttribute("selectedSubjectId", subjectId);
        assertEquals("statistics/index", viewName);
    }
} 