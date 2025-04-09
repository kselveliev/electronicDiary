package com.ednevnik.dnevnik.controller;

import com.ednevnik.dnevnik.model.*;
import com.ednevnik.dnevnik.repository.ClassRepository;
import com.ednevnik.dnevnik.repository.SchoolRepository;
import com.ednevnik.dnevnik.repository.StudentRepository;
import com.ednevnik.dnevnik.repository.TeacherRepository;
import com.ednevnik.dnevnik.repository.ClassAssignmentRepository;
import com.ednevnik.dnevnik.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClassControllerTest {

    @Mock
    private ClassRepository classRepository;

    @Mock
    private SchoolRepository schoolRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private ClassAssignmentRepository classAssignmentRepository;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetailsImpl userDetails;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private ClassController classController;

    private School school;
    private Teacher teacher;
    private Student student;
    private com.ednevnik.dnevnik.model.Class schoolClass;
    private Director director;
    private List<Teacher> teachers;
    private List<com.ednevnik.dnevnik.model.Class> classes;
    private List<School> schools;

    @BeforeEach
    void setUp() {
        // Setup test data
        school = new School();
        school.setId(1L);
        school.setName("Test School");

        teacher = new Teacher();
        teacher.setId(1L);
        teacher.setFirstName("John");
        teacher.setLastName("Doe");
        teacher.setSchool(school);

        student = new Student();
        student.setId(1L);
        student.setFirstName("Jane");
        student.setLastName("Smith");

        schoolClass = new com.ednevnik.dnevnik.model.Class();
        schoolClass.setId(1L);
        schoolClass.setName("Class A");
        schoolClass.setGrade(10);
        schoolClass.setSchool(school);
        schoolClass.setClassTeacher(teacher);
        schoolClass.setStudents(new HashSet<>(Collections.singletonList(student)));

        director = new Director();
        director.setId(1L);
        director.setFirstName("Bob");
        director.setLastName("Johnson");
        director.setSchool(school);
        director.setRole(UserRole.ROLE_DIRECTOR);
        
        // Set up the bidirectional relationship
        school.setDirector(director);

        teachers = Collections.singletonList(teacher);
        classes = Collections.singletonList(schoolClass);
        schools = Collections.singletonList(school);
    }

    @Test
    void listClassesShouldShowAllClassesForAdmin() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(new User() {{
            setRole(UserRole.ROLE_ADMIN);
        }});
        when(classRepository.findAll()).thenReturn(classes);
        when(schoolRepository.findAll()).thenReturn(schools);

        // When
        String viewName = classController.listClasses(model, authentication);

        // Then
        verify(classRepository).findAll();
        verify(schoolRepository).findAll();
        verify(model).addAttribute("classes", classes);
        verify(model).addAttribute("schools", schools);
        verify(model).addAttribute(any(), any(com.ednevnik.dnevnik.model.Class.class));
        assertEquals("classes/list", viewName);
    }

    @Test
    void listClassesShouldShowSchoolClassesForDirector() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(director);
        when(classRepository.findBySchoolId(1L)).thenReturn(classes);
        
        // When
        String viewName = classController.listClasses(model, authentication);

        // Then
        verify(classRepository).findBySchoolId(director.getSchool().getId());
        verify(model).addAttribute("classes", classes);
        verify(model).addAttribute("schools", Collections.singletonList(school));
        verify(model).addAttribute(any(), any(com.ednevnik.dnevnik.model.Class.class));
        assertEquals("classes/list", viewName);
    }

    @Test
    void getTeachersBySchoolShouldReturnTeachersList() {
        // Given
        when(teacherRepository.findBySchoolId(1L)).thenReturn(teachers);

        // When
        ResponseEntity<List<TeacherDTO>> response = classController.getTeachersBySchool(1L);

        // Then
        verify(teacherRepository).findBySchoolId(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).id());
        assertEquals("John", response.getBody().get(0).firstName());
        assertEquals("Doe", response.getBody().get(0).lastName());
    }

    @Test
    void showClassShouldReturnClassDetails() {
        // Given
        when(classRepository.findById(1L)).thenReturn(Optional.of(schoolClass));
        when(studentRepository.findAll()).thenReturn(Collections.singletonList(student));

        // When
        String viewName = classController.showClass(1L, model);

        // Then
        verify(classRepository).findById(1L);
        verify(studentRepository).findAll();
        verify(model).addAttribute("schoolClass", schoolClass);
        verify(model).addAttribute("availableStudents", Collections.singletonList(student));
        assertEquals("classes/details", viewName);
    }

    @Test
    void createClassShouldCreateNewClass() {
        // Given
        when(classRepository.save(any(com.ednevnik.dnevnik.model.Class.class))).thenReturn(schoolClass);

        // When
        String redirectUrl = classController.createClass(schoolClass, redirectAttributes);

        // Then
        verify(classRepository).save(schoolClass);
        verify(redirectAttributes).addFlashAttribute("success", "Class created successfully");
        assertEquals("redirect:/classes", redirectUrl);
    }

    @Test
    void deleteClassShouldDeleteClass() {
        // Given
        doNothing().when(classAssignmentRepository).deleteBySchoolClassId(1L);
        doNothing().when(classRepository).deleteById(1L);

        // When
        String redirectUrl = classController.deleteClass(1L, redirectAttributes);

        // Then
        verify(classAssignmentRepository).deleteBySchoolClassId(1L);
        verify(classRepository).deleteById(1L);
        verify(redirectAttributes).addFlashAttribute("success", "Class deleted successfully");
        assertEquals("redirect:/classes", redirectUrl);
    }

    @Test
    void updateClassShouldUpdateExistingClass() {
        // Given
        when(classRepository.findById(1L)).thenReturn(Optional.of(schoolClass));
        when(classRepository.save(any(com.ednevnik.dnevnik.model.Class.class))).thenReturn(schoolClass);

        com.ednevnik.dnevnik.model.Class updatedClass = new com.ednevnik.dnevnik.model.Class();
        updatedClass.setName("Updated Class");
        updatedClass.setGrade(11);
        updatedClass.setSchool(school);
        updatedClass.setClassTeacher(teacher);

        // When
        String redirectUrl = classController.updateClass(1L, updatedClass, redirectAttributes);

        // Then
        verify(classRepository).findById(1L);
        verify(classRepository).save(any(com.ednevnik.dnevnik.model.Class.class));
        verify(redirectAttributes).addFlashAttribute("success", "Class updated successfully");
        assertEquals("redirect:/classes", redirectUrl);
    }

    @Test
    void assignStudentShouldAssignStudentToClass() {
        // Given
        when(classRepository.findById(1L)).thenReturn(Optional.of(schoolClass));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        // When
        String redirectUrl = classController.assignStudent(1L, 1L, redirectAttributes);

        // Then
        verify(classRepository).findById(1L);
        verify(studentRepository).findById(1L);
        verify(studentRepository).save(student);
        verify(redirectAttributes).addFlashAttribute("success", "Student assigned successfully");
        assertEquals("redirect:/classes/1", redirectUrl);
    }

    @Test
    void removeStudentShouldRemoveStudentFromClass() {
        // Given
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        // When
        String redirectUrl = classController.removeStudent(1L, 1L, redirectAttributes);

        // Then
        verify(studentRepository).findById(1L);
        verify(studentRepository).save(student);
        verify(redirectAttributes).addFlashAttribute("success", "Student removed successfully");
        assertEquals("redirect:/classes/1", redirectUrl);
    }
} 