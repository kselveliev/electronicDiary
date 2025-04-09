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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurriculumControllerTest {

    @Mock
    private CurriculumRepository curriculumRepository;

    @Mock
    private CurriculumSubjectRepository curriculumSubjectRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private SchoolRepository schoolRepository;

    @Mock
    private ClassRepository classRepository;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetailsImpl userDetails;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private CurriculumController curriculumController;

    private School school;
    private Curriculum curriculum;
    private Subject subject;
    private Teacher teacher;
    private com.ednevnik.dnevnik.model.Class studentClass;
    private CurriculumSubject curriculumSubject;
    private Parent parent;
    private Student student;

    @BeforeEach
    void setUp() {
        // Setup test data
        school = new School();
        school.setId(1L);
        school.setName("Test School");

        curriculum = new Curriculum();
        curriculum.setId(1L);
        curriculum.setName("Test Curriculum");
        curriculum.setGrade(10);
        curriculum.setStartDate(LocalDate.now());
        curriculum.setEndDate(LocalDate.now().plusYears(1));
        curriculum.setSchool(school);
        curriculum.setActive(true);

        subject = new Subject();
        subject.setId(1L);
        subject.setName("Mathematics");

        teacher = new Teacher();
        teacher.setId(1L);
        teacher.setSchool(school);

        studentClass = new com.ednevnik.dnevnik.model.Class();
        studentClass.setId(1L);
        studentClass.setName("Class A");
        studentClass.setSchool(school);
        studentClass.setGrade(10);

        curriculumSubject = new CurriculumSubject();
        curriculumSubject.setId(1L);
        curriculumSubject.setCurriculum(curriculum);
        curriculumSubject.setSubject(subject);
        curriculumSubject.setTeacher(teacher);
        curriculumSubject.setSchoolClass(studentClass);
        curriculumSubject.setDayOfWeek(DayOfWeek.MONDAY);
        curriculumSubject.setLessonNumber(1);

        parent = new Parent();
        parent.setId(1L);
        Set<Student> children = new HashSet<>();
        student = new Student();
        student.setId(1L);
        student.setStudentClass(studentClass);
        children.add(student);
        parent.setChildren(children);
    }

    @Test
    void listCurriculumsShouldReturnListViewForAdmin() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(new User() {{
            setRole(UserRole.ROLE_ADMIN);
        }});
        when(curriculumRepository.findAll()).thenReturn(Arrays.asList(curriculum));

        // When
        String viewName = curriculumController.listCurriculums(model, authentication);

        // Then
        verify(curriculumRepository).findAll();
        verify(model).addAttribute("curriculums", Arrays.asList(curriculum));
        verify(model).addAttribute("canEdit", true);
        assertEquals("curriculum/list", viewName);
    }

    @Test
    void listCurriculumsShouldReturnListViewForDirector() {
        // Given
        Director director = new Director();
        director.setId(1L);
        director.setSchool(school);
        director.setRole(UserRole.ROLE_DIRECTOR);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(director);
        when(curriculumRepository.findBySchoolId(1L)).thenReturn(Arrays.asList(curriculum));

        // When
        String viewName = curriculumController.listCurriculums(model, authentication);

        // Then
        verify(curriculumRepository).findBySchoolId(1L);
        verify(model).addAttribute("curriculums", Arrays.asList(curriculum));
        verify(model).addAttribute("canEdit", true);
        assertEquals("curriculum/list", viewName);
    }

    @Test
    void showNewCurriculumFormShouldReturnFormViewForAdmin() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(new User() {{
            setRole(UserRole.ROLE_ADMIN);
        }});
        when(schoolRepository.findAll()).thenReturn(Arrays.asList(school));

        // When
        String viewName = curriculumController.showNewCurriculumForm(model, authentication);

        // Then
        verify(schoolRepository).findAll();
        verify(model).addAttribute("schools", Arrays.asList(school));
        assertEquals("curriculum/form", viewName);
    }

    @Test
    void createCurriculumShouldReturnRedirect() {
        // Given
        when(schoolRepository.findById(1L)).thenReturn(java.util.Optional.of(school));
        when(curriculumRepository.save(any(Curriculum.class))).thenReturn(curriculum);

        // When
        String redirectUrl = curriculumController.createCurriculum(
            "Test Curriculum", 10, 
            LocalDate.now().toString(), 
            LocalDate.now().plusYears(1).toString(),
            1L, redirectAttributes
        );

        // Then
        verify(schoolRepository).findById(1L);
        verify(curriculumRepository).save(any(Curriculum.class));
        verify(redirectAttributes).addFlashAttribute("success", "Curriculum created successfully");
        assertEquals("redirect:/curriculum", redirectUrl);
    }

    @Test
    void showCurriculumShouldReturnDetailsView() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(new User() {{
            setRole(UserRole.ROLE_ADMIN);
        }});
        when(curriculumRepository.findById(1L)).thenReturn(java.util.Optional.of(curriculum));
        when(subjectRepository.findAll()).thenReturn(Arrays.asList(subject));
        when(teacherRepository.findBySchoolId(1L)).thenReturn(Arrays.asList(teacher));
        when(classRepository.findBySchoolIdAndGrade(1L, 10)).thenReturn(Arrays.asList(studentClass));
        when(curriculumSubjectRepository.findByCurriculumId(1L)).thenReturn(Arrays.asList(curriculumSubject));

        // When
        String viewName = curriculumController.showCurriculum(1L, model, authentication);

        // Then
        verify(curriculumRepository).findById(1L);
        verify(subjectRepository).findAll();
        verify(teacherRepository).findBySchoolId(1L);
        verify(classRepository).findBySchoolIdAndGrade(1L, 10);
        verify(curriculumSubjectRepository).findByCurriculumId(1L);
        verify(model).addAttribute("curriculum", curriculum);
        verify(model).addAttribute("subjects", Arrays.asList(subject));
        verify(model).addAttribute("teachers", Arrays.asList(teacher));
        verify(model).addAttribute("classes", Arrays.asList(studentClass));
        verify(model).addAttribute("curriculumSubjects", Arrays.asList(curriculumSubject));
        verify(model).addAttribute("canEdit", true);
        assertEquals("curriculum/details", viewName);
    }

    @Test
    void addSubjectToCurriculumShouldReturnRedirect() {
        // Given
        when(curriculumRepository.findById(1L)).thenReturn(java.util.Optional.of(curriculum));
        when(classRepository.findById(1L)).thenReturn(java.util.Optional.of(studentClass));
        when(subjectRepository.findById(1L)).thenReturn(java.util.Optional.of(subject));
        when(teacherRepository.findById(1L)).thenReturn(java.util.Optional.of(teacher));
        when(curriculumSubjectRepository.findByCurriculumId(1L)).thenReturn(Arrays.asList());
        when(curriculumSubjectRepository.save(any(CurriculumSubject.class))).thenReturn(curriculumSubject);

        // When
        String redirectUrl = curriculumController.addSubjectToCurriculum(
            1L, 1L, 1L, 1L, DayOfWeek.MONDAY, 1, redirectAttributes
        );

        // Then
        verify(curriculumRepository).findById(1L);
        verify(classRepository).findById(1L);
        verify(subjectRepository).findById(1L);
        verify(teacherRepository).findById(1L);
        verify(curriculumSubjectRepository).save(any(CurriculumSubject.class));
        verify(redirectAttributes).addFlashAttribute("success", "Subject added to schedule successfully");
        assertEquals("redirect:/curriculum/1", redirectUrl);
    }

    @Test
    void removeSubjectFromCurriculumShouldReturnRedirect() {
        // Given
        when(curriculumSubjectRepository.findById(1L)).thenReturn(java.util.Optional.of(curriculumSubject));

        // When
        String redirectUrl = curriculumController.removeSubjectFromCurriculum(1L, 1L, redirectAttributes);

        // Then
        verify(curriculumSubjectRepository).findById(1L);
        verify(curriculumSubjectRepository).delete(curriculumSubject);
        verify(redirectAttributes).addFlashAttribute("success", "Subject removed from schedule successfully");
        assertEquals("redirect:/curriculum/1", redirectUrl);
    }

    @Test
    void toggleCurriculumActiveShouldReturnRedirect() {
        // Given
        when(curriculumRepository.findById(1L)).thenReturn(java.util.Optional.of(curriculum));
        when(curriculumRepository.save(any(Curriculum.class))).thenReturn(curriculum);

        // When
        String redirectUrl = curriculumController.toggleCurriculumActive(1L, redirectAttributes);

        // Then
        verify(curriculumRepository).findById(1L);
        verify(curriculumRepository).save(any(Curriculum.class));
        verify(redirectAttributes).addFlashAttribute("success", "Curriculum deactivated successfully");
        assertEquals("redirect:/curriculum", redirectUrl);
    }

    @Test
    void editCurriculumShouldReturnFormViewForAdmin() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(new User() {{
            setRole(UserRole.ROLE_ADMIN);
        }});
        when(curriculumRepository.findById(1L)).thenReturn(java.util.Optional.of(curriculum));
        when(schoolRepository.findAll()).thenReturn(Arrays.asList(school));

        // When
        String viewName = curriculumController.editCurriculum(1L, model, authentication);

        // Then
        verify(curriculumRepository).findById(1L);
        verify(schoolRepository).findAll();
        verify(model).addAttribute("curriculum", curriculum);
        verify(model).addAttribute("schools", Arrays.asList(school));
        assertEquals("curriculum/edit", viewName);
    }

    @Test
    void editCurriculumShouldReturnFormViewForDirector() {
        // Given
        Director director = new Director();
        director.setId(1L);
        director.setSchool(school);
        director.setRole(UserRole.ROLE_DIRECTOR);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(director);
        when(curriculumRepository.findById(1L)).thenReturn(java.util.Optional.of(curriculum));
        when(schoolRepository.findAll()).thenReturn(Arrays.asList(school));

        // When
        String viewName = curriculumController.editCurriculum(1L, model, authentication);

        // Then
        verify(curriculumRepository).findById(1L);
        verify(schoolRepository).findAll();
        verify(model).addAttribute("curriculum", curriculum);
        verify(model).addAttribute("schools", Arrays.asList(school));
        assertEquals("curriculum/edit", viewName);
    }

    @Test
    void updateCurriculumShouldReturnRedirect() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(new User() {{
            setRole(UserRole.ROLE_ADMIN);
        }});
        when(curriculumRepository.findById(1L)).thenReturn(java.util.Optional.of(curriculum));
        when(schoolRepository.findById(1L)).thenReturn(java.util.Optional.of(school));
        when(curriculumRepository.save(any(Curriculum.class))).thenReturn(curriculum);

        // When
        String redirectUrl = curriculumController.updateCurriculum(
            1L, "Updated Curriculum", 11,
            LocalDate.now().toString(),
            LocalDate.now().plusYears(1).toString(),
            1L, true, redirectAttributes, authentication
        );

        // Then
        verify(schoolRepository).findById(1L);
        verify(curriculumRepository).findById(1L);
        verify(curriculumRepository).save(any(Curriculum.class));
        verify(redirectAttributes).addFlashAttribute("success", "Curriculum updated successfully");
        assertEquals("redirect:/curriculum", redirectUrl);
    }

    @Test
    void updateCurriculumShouldHandleError() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(new User() {{
            setRole(UserRole.ROLE_ADMIN);
        }});
        when(curriculumRepository.findById(1L)).thenReturn(java.util.Optional.of(curriculum));
        when(schoolRepository.findById(1L)).thenReturn(java.util.Optional.of(school));
        when(curriculumRepository.save(any(Curriculum.class))).thenThrow(new RuntimeException("Database error"));

        // When
        String redirectUrl = curriculumController.updateCurriculum(
            1L, "Updated Curriculum", 11,
            LocalDate.now().toString(),
            LocalDate.now().plusYears(1).toString(),
            1L, true, redirectAttributes, authentication
        );

        // Then
        verify(schoolRepository).findById(1L);
        verify(curriculumRepository).findById(1L);
        verify(curriculumRepository).save(any(Curriculum.class));
        verify(redirectAttributes).addFlashAttribute("error", "Error updating curriculum: Database error");
        assertEquals("redirect:/curriculum/1/edit", redirectUrl);
    }
} 