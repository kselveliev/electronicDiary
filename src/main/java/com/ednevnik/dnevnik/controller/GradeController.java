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
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;
    private final ClassAssignmentRepository classAssignmentRepository;
    private final StudentRepository studentRepository;
    private final GradeRepository gradeRepository;
    private final ClassRepository classRepository;

    @GetMapping({"/grades", "/my-grades"})
    public String showGrades(@RequestParam(required = false) Long studentId,
                           @RequestParam(required = false) Long classId,
                           Model model, 
                           Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userDetails.getUser();
        List<GradeDto> grades = List.of(); // Initialize as empty list

        if (user instanceof Student) {
            grades = gradeService.getGradesForStudent(user.getId());
            model.addAttribute("studentName", user.getFirstName() + " " + user.getLastName());
        } else if (user instanceof Parent) {
            Parent parent = (Parent) user;
            if (parent.getChildren() == null || parent.getChildren().isEmpty()) {
                model.addAttribute("message", "No children found in your account.");
            } else if (studentId != null) {
                // Find the specific child
                Student selectedChild = parent.getChildren().stream()
                    .filter(child -> child.getId().equals(studentId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Child not found or not associated with parent"));
                
                grades = gradeService.getGradesForStudent(selectedChild.getId());
                model.addAttribute("studentName", selectedChild.getFirstName() + " " + selectedChild.getLastName());
                model.addAttribute("studentId", studentId);
            } else {
                // Redirect to children list if no specific child is selected
                return "redirect:/children";
            }
        } else {
            // For teachers
            if (user instanceof Teacher) {
                Teacher teacher = (Teacher) user;
                List<ClassAssignment> assignments = classAssignmentRepository.findByTeacherId(teacher.getId());
                
                // Get unique subjects from assignments
                Set<Subject> teacherSubjects = assignments.stream()
                    .map(ClassAssignment::getSubject)
                    .collect(Collectors.toSet());
                model.addAttribute("subjects", teacherSubjects);

                // Get unique classes from assignments
                Set<com.ednevnik.dnevnik.model.Class> teacherClasses = assignments.stream()
                    .map(ClassAssignment::getSchoolClass)
                    .collect(Collectors.toSet());
                model.addAttribute("classes", teacherClasses);

                // Get all grades assigned by this teacher
                grades = gradeRepository.findByTeacherId(teacher.getId()).stream()
                    .map(grade -> {
                        GradeDto dto = new GradeDto();
                        dto.setId(grade.getId());
                        dto.setGrade(grade.getGrade());
                        dto.setStudentId(grade.getStudent().getId());
                        dto.setStudentName(grade.getStudent().getFirstName() + " " + grade.getStudent().getLastName());
                        dto.setSubjectId(grade.getSubject().getId());
                        dto.setSubjectName(grade.getSubject().getName());
                        dto.setTeacherId(grade.getTeacher().getId());
                        dto.setTeacherName(grade.getTeacher().getFirstName() + " " + grade.getTeacher().getLastName());
                        dto.setCreatedTimestamp(grade.getCreatedTimestamp());
                        dto.setClassName(grade.getStudent().getStudentClass().getName());
                        return dto;
                    })
                    .collect(Collectors.toList());

                if (grades.isEmpty()) {
                    model.addAttribute("message", "No grades found. Use the form above to add grades.");
                }
            } else {
                // For admins and directors
                model.addAttribute("message", "Please use the search to find a student's grades.");
                model.addAttribute("showSearch", true);
            }
        }

        model.addAttribute("grades", grades);
        model.addAttribute("isTeacher", user instanceof Teacher);
        model.addAttribute("isAdminOrDirector", user.getRole() == UserRole.ROLE_ADMIN || user.getRole() == UserRole.ROLE_DIRECTOR);
        return "grades/list";
    }

    @GetMapping({"/grades/subject/{subjectId}", "/my-grades/subject/{subjectId}"})
    public String showGradesBySubject(@PathVariable Long subjectId, 
                                    @RequestParam(required = false) Long studentId,
                                    Model model, 
                                    Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userDetails.getUser();
        List<GradeDto> grades;

        if (user instanceof Student) {
            grades = gradeService.getGradesForStudentBySubject(user.getId(), subjectId);
            model.addAttribute("studentName", user.getFirstName() + " " + user.getLastName());
        } else if (user instanceof Parent) {
            Parent parent = (Parent) user;
            if (parent.getChildren() == null || parent.getChildren().isEmpty()) {
                grades = List.of();
                model.addAttribute("message", "No children found in your account.");
            } else if (studentId != null) {
                // Find the specific child
                Student selectedChild = parent.getChildren().stream()
                    .filter(child -> child.getId().equals(studentId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Child not found or not associated with parent"));
                
                grades = gradeService.getGradesForStudentBySubject(selectedChild.getId(), subjectId);
                model.addAttribute("studentName", selectedChild.getFirstName() + " " + selectedChild.getLastName());
                model.addAttribute("studentId", studentId);
            } else {
                // Redirect to children list if no specific child is selected
                return "redirect:/children";
            }
        } else {
            throw new RuntimeException("Unauthorized access");
        }

        model.addAttribute("grades", grades);
        model.addAttribute("subjectId", subjectId);
        return "grades/list";
    }

    @PostMapping("/grades")
    public String addGrade(@RequestParam Long studentId, 
                          @RequestParam Long subjectId, 
                          @RequestParam Integer grade,
                          @RequestParam Long teacherId,
                          Authentication authentication,
                          RedirectAttributes redirectAttributes) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userDetails.getUser();
        
        // Create a GradeDto from the form data
        GradeDto gradeDto = new GradeDto();
        gradeDto.setStudentId(studentId);
        gradeDto.setSubjectId(subjectId);
        gradeDto.setGrade(grade);
        gradeDto.setTeacherId(teacherId);
        
        try {
            // Check if user is admin or director
            if (user.getRole() == UserRole.ROLE_ADMIN || user.getRole() == UserRole.ROLE_DIRECTOR) {
                gradeService.addGrade(gradeDto);
                redirectAttributes.addFlashAttribute("success", "Grade added successfully");
            }
            // Check if user is a teacher and teaches the student
            else if (user instanceof Teacher) {
                Teacher teacher = (Teacher) user;
                // Verify if the teacher is assigned to the student's class/subject
                if (isTeacherAuthorizedForStudent(teacher, studentId)) {
                    gradeService.addGrade(gradeDto);
                    redirectAttributes.addFlashAttribute("success", "Grade added successfully");
                } else {
                    redirectAttributes.addFlashAttribute("error", "You are not authorized to add grades for this student");
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Unauthorized: Only teachers of the student, directors, or admins can add grades");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error adding grade: " + e.getMessage());
        }
        
        return "redirect:/grades";
    }

    @PostMapping("/grades/{id}")
    public String deleteGrade(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userDetails.getUser();
        
        try {
            // Check if user is admin or director
            if (user.getRole() == UserRole.ROLE_ADMIN || user.getRole() == UserRole.ROLE_DIRECTOR) {
                gradeService.deleteGrade(id);
                redirectAttributes.addFlashAttribute("success", "Grade deleted successfully");
            }
            // Check if user is a teacher and owns the grade
            else if (user instanceof Teacher) {
                Teacher teacher = (Teacher) user;
                if (isTeacherAuthorizedForGrade(teacher, id)) {
                    gradeService.deleteGrade(id);
                    redirectAttributes.addFlashAttribute("success", "Grade deleted successfully");
                } else {
                    redirectAttributes.addFlashAttribute("error", "You are not authorized to delete this grade");
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Unauthorized: Only the teacher who created the grade, directors, or admins can delete grades");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting grade: " + e.getMessage());
        }
        
        return "redirect:/grades";
    }

    private boolean isTeacherAuthorizedForStudent(Teacher teacher, Long studentId) {
        // Get all class assignments for the teacher
        List<ClassAssignment> teacherAssignments = classAssignmentRepository.findByTeacherId(teacher.getId());
        
        // Get the student
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found"));
            
        // Check if the student is in any of the teacher's assigned classes
        return teacherAssignments.stream()
            .map(ClassAssignment::getSchoolClass)
            .anyMatch(schoolClass -> schoolClass.getStudents().contains(student));
    }

    private boolean isTeacherAuthorizedForGrade(Teacher teacher, Long gradeId) {
        // Get the grade
        Grade grade = gradeRepository.findById(gradeId)
            .orElseThrow(() -> new RuntimeException("Grade not found"));
            
        // Check if the teacher created the grade
        return grade.getTeacher().getId().equals(teacher.getId());
    }

    @GetMapping("/grades/students-by-class/{classId}")
    @ResponseBody
    public List<StudentDto> getStudentsByClass(@PathVariable Long classId) {
        // Get the class
        com.ednevnik.dnevnik.model.Class schoolClass = classRepository.findById(classId)
            .orElseThrow(() -> new RuntimeException("Class not found"));
            
        // Get students from the class
        return schoolClass.getStudents().stream()
            .map(student -> new StudentDto(student.getId(), student.getFirstName(), student.getLastName()))
            .collect(Collectors.toList());
    }
} 