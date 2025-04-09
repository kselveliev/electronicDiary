package com.ednevnik.dnevnik.controller;

import com.ednevnik.dnevnik.dto.GradeDto;
import com.ednevnik.dnevnik.dto.StudentDto;
import com.ednevnik.dnevnik.model.*;
import com.ednevnik.dnevnik.repository.ClassAssignmentRepository;
import com.ednevnik.dnevnik.repository.StudentRepository;
import com.ednevnik.dnevnik.repository.GradeRepository;
import com.ednevnik.dnevnik.repository.ClassRepository;
import com.ednevnik.dnevnik.security.UserDetailsImpl;
import com.ednevnik.dnevnik.service.GradeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GradeControllerTest {

    @Mock
    private GradeService gradeService;

    @Mock
    private ClassAssignmentRepository classAssignmentRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private GradeRepository gradeRepository;

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
    private GradeController gradeController;

    private Student student;
    private Parent parent;
    private Teacher teacher;
    private Subject subject;
    private com.ednevnik.dnevnik.model.Class schoolClass;
    private Grade grade;
    private List<GradeDto> gradeDtos;
    private Set<Student> children;
    private List<ClassAssignment> classAssignments;

    @BeforeEach
    void setUp() {
        // Setup test data
        student = new Student();
        student.setId(1L);
        student.setFirstName("John");
        student.setLastName("Doe");
        student.setStudentClass(schoolClass);

        parent = new Parent();
        parent.setId(1L);
        parent.setFirstName("Jane");
        parent.setLastName("Doe");

        teacher = new Teacher();
        teacher.setId(1L);
        teacher.setFirstName("Bob");
        teacher.setLastName("Smith");

        subject = new Subject();
        subject.setId(1L);
        subject.setName("Math");

        schoolClass = new com.ednevnik.dnevnik.model.Class();
        schoolClass.setId(1L);
        schoolClass.setName("Class A");
        schoolClass.setStudents(new HashSet<>(Collections.singletonList(student)));
        student.setStudentClass(schoolClass);

        grade = new Grade();
        grade.setId(1L);
        grade.setGrade(5);
        grade.setStudent(student);
        grade.setSubject(subject);
        grade.setTeacher(teacher);

        GradeDto gradeDto = new GradeDto();
        gradeDto.setId(1L);
        gradeDto.setGrade(5);
        gradeDto.setStudentId(1L);
        gradeDto.setStudentName("John Doe");
        gradeDto.setSubjectId(1L);
        gradeDto.setSubjectName("Math");
        gradeDto.setTeacherId(1L);
        gradeDto.setTeacherName("Bob Smith");
        gradeDto.setClassName("Class A");
        gradeDtos = Collections.singletonList(gradeDto);

        children = new HashSet<>(Collections.singletonList(student));
        parent.setChildren(children);

        ClassAssignment classAssignment = new ClassAssignment();
        classAssignment.setId(1L);
        classAssignment.setTeacher(teacher);
        classAssignment.setSubject(subject);
        classAssignment.setSchoolClass(schoolClass);
        classAssignments = Collections.singletonList(classAssignment);
    }

    @Test
    void showGradesShouldReturnListViewForStudent() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(student);
        when(gradeService.getGradesForStudent(1L)).thenReturn(gradeDtos);

        // When
        String viewName = gradeController.showGrades(null, null, null, model, authentication);

        // Then
        verify(gradeService).getGradesForStudent(1L);
        verify(model).addAttribute("grades", gradeDtos);
        verify(model).addAttribute("studentName", "John Doe");
        verify(model).addAttribute("isTeacher", false);
        verify(model).addAttribute("isAdminOrDirector", false);
        assertEquals("grades/list", viewName);
    }

    @Test
    void showGradesShouldReturnListViewForParent() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(parent);
        when(gradeService.getGradesForStudent(1L)).thenReturn(gradeDtos);

        // When
        String viewName = gradeController.showGrades(1L, null, null, model, authentication);

        // Then
        verify(gradeService).getGradesForStudent(1L);
        verify(model).addAttribute("grades", gradeDtos);
        verify(model).addAttribute("studentName", "John Doe");
        verify(model).addAttribute("studentId", 1L);
        verify(model).addAttribute("isTeacher", false);
        verify(model).addAttribute("isAdminOrDirector", false);
        assertEquals("grades/list", viewName);
    }

    @Test
    void showGradesShouldRedirectToChildrenForParentWithoutSelectedChild() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(parent);

        // When
        String viewName = gradeController.showGrades(null, null, null, model, authentication);

        // Then
        assertEquals("redirect:/children", viewName);
    }

    @Test
    void showGradesShouldReturnListViewForTeacher() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(teacher);
        when(classAssignmentRepository.findByTeacherId(1L)).thenReturn(classAssignments);
        when(gradeRepository.findByTeacherId(1L)).thenReturn(Collections.singletonList(grade));

        // When
        String viewName = gradeController.showGrades(null, null, null, model, authentication);

        // Then
        verify(classAssignmentRepository).findByTeacherId(1L);
        verify(gradeRepository).findByTeacherId(1L);
        verify(model).addAttribute("subjects", Collections.singleton(subject));
        verify(model).addAttribute("classes", Collections.singleton(schoolClass));
        verify(model).addAttribute("grades", gradeDtos);
        verify(model).addAttribute("isTeacher", true);
        verify(model).addAttribute("isAdminOrDirector", false);
        assertEquals("grades/list", viewName);
    }

    @Test
    void showGradesBySubjectShouldReturnListViewForStudent() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(student);
        when(gradeService.getGradesForStudentBySubject(1L, 1L)).thenReturn(gradeDtos);

        // When
        String viewName = gradeController.showGradesBySubject(1L, null, model, authentication);

        // Then
        verify(gradeService).getGradesForStudentBySubject(1L, 1L);
        verify(model).addAttribute("grades", gradeDtos);
        verify(model).addAttribute("studentName", "John Doe");
        verify(model).addAttribute("subjectId", 1L);
        assertEquals("grades/list", viewName);
    }

    @Test
    void addGradeShouldAddGradeForAdmin() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(new User() {{
            setRole(UserRole.ROLE_ADMIN);
        }});
        when(gradeService.addGrade(any(GradeDto.class))).thenReturn(new GradeDto());

        // When
        String redirectUrl = gradeController.addGrade(1L, 1L, 5, 1L, authentication, redirectAttributes);

        // Then
        verify(gradeService).addGrade(any(GradeDto.class));
        verify(redirectAttributes).addFlashAttribute("success", "Grade added successfully");
        assertEquals("redirect:/grades", redirectUrl);
    }

    @Test
    void addGradeShouldAddGradeForAuthorizedTeacher() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(teacher);
        when(classAssignmentRepository.findByTeacherId(1L)).thenReturn(classAssignments);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(gradeService.addGrade(any(GradeDto.class))).thenReturn(new GradeDto());

        // When
        String redirectUrl = gradeController.addGrade(1L, 1L, 5, 1L, authentication, redirectAttributes);

        // Then
        verify(gradeService).addGrade(any(GradeDto.class));
        verify(redirectAttributes).addFlashAttribute("success", "Grade added successfully");
        assertEquals("redirect:/grades", redirectUrl);
    }

    @Test
    void deleteGradeShouldDeleteGradeForAdmin() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(new User() {{
            setRole(UserRole.ROLE_ADMIN);
        }});

        // When
        String redirectUrl = gradeController.deleteGrade(1L, authentication, redirectAttributes);

        // Then
        verify(gradeService).deleteGrade(1L);
        verify(redirectAttributes).addFlashAttribute("success", "Grade deleted successfully");
        assertEquals("redirect:/grades", redirectUrl);
    }

    @Test
    void deleteGradeShouldDeleteGradeForAuthorizedTeacher() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(teacher);
        when(gradeRepository.findById(1L)).thenReturn(Optional.of(grade));

        // When
        String redirectUrl = gradeController.deleteGrade(1L, authentication, redirectAttributes);

        // Then
        verify(gradeService).deleteGrade(1L);
        verify(redirectAttributes).addFlashAttribute("success", "Grade deleted successfully");
        assertEquals("redirect:/grades", redirectUrl);
    }

    @Test
    void getStudentsByClassShouldReturnStudentsList() {
        // Given
        when(classRepository.findById(1L)).thenReturn(Optional.of(schoolClass));

        // When
        List<StudentDto> students = gradeController.getStudentsByClass(1L);

        // Then
        verify(classRepository).findById(1L);
        assertEquals(1, students.size());
        assertEquals(1L, students.get(0).getId());
        assertEquals("John", students.get(0).getFirstName());
        assertEquals("Doe", students.get(0).getLastName());
    }
} 