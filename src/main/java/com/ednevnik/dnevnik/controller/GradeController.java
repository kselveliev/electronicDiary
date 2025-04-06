package com.ednevnik.dnevnik.controller;

import com.ednevnik.dnevnik.dto.GradeDto;
import com.ednevnik.dnevnik.model.*;
import com.ednevnik.dnevnik.security.UserDetailsImpl;
import com.ednevnik.dnevnik.service.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    @GetMapping({"/grades", "/my-grades"})
    public String showGrades(@RequestParam(required = false) Long studentId, Model model, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userDetails.getUser();
        List<GradeDto> grades;

        if (user instanceof Student) {
            grades = gradeService.getGradesForStudent(user.getId());
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
                
                grades = gradeService.getGradesForStudent(selectedChild.getId());
                model.addAttribute("studentName", selectedChild.getFirstName() + " " + selectedChild.getLastName());
                model.addAttribute("studentId", studentId);
            } else {
                // Redirect to children list if no specific child is selected
                return "redirect:/children";
            }
        } else if (user instanceof Teacher || user.getRole() == UserRole.ROLE_ADMIN || user.getRole() == UserRole.ROLE_DIRECTOR) {
            // For teachers, admins, and directors, show an empty list initially
            // They will use a search interface to find specific students
            grades = List.of();
            model.addAttribute("message", "Please use the search to find a student's grades.");
            model.addAttribute("showSearch", true);
        } else {
            throw new RuntimeException("Unauthorized access");
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
    @ResponseBody
    public GradeDto addGrade(@RequestBody GradeDto gradeDto, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userDetails.getUser();
        
        // Check if user is admin or director
        if (user.getRole() == UserRole.ROLE_ADMIN || user.getRole() == UserRole.ROLE_DIRECTOR) {
            return gradeService.addGrade(gradeDto);
        }
        
        // Check if user is a teacher and teaches the student
        if (user instanceof Teacher) {
            Teacher teacher = (Teacher) user;
            // Verify if the teacher is assigned to the student's class/subject
            if (isTeacherAuthorizedForStudent(teacher, gradeDto.getStudentId())) {
                return gradeService.addGrade(gradeDto);
            }
        }
        
        throw new RuntimeException("Unauthorized: Only teachers of the student, directors, or admins can add grades");
    }

    @DeleteMapping("/grades/{id}")
    @ResponseBody
    public void deleteGrade(@PathVariable Long id, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userDetails.getUser();
        
        // Check if user is admin or director
        if (user.getRole() == UserRole.ROLE_ADMIN || user.getRole() == UserRole.ROLE_DIRECTOR) {
            gradeService.deleteGrade(id);
            return;
        }
        
        // Check if user is a teacher and owns the grade
        if (user instanceof Teacher) {
            Teacher teacher = (Teacher) user;
            if (isTeacherAuthorizedForGrade(teacher, id)) {
                gradeService.deleteGrade(id);
                return;
            }
        }
        
        throw new RuntimeException("Unauthorized: Only the teacher who created the grade, directors, or admins can delete grades");
    }

    private boolean isTeacherAuthorizedForStudent(Teacher teacher, Long studentId) {
        // TODO: Implement logic to check if teacher is assigned to student's class/subject
        // This could involve checking teacher's subjects, classes, or direct student assignments
        return true; // Placeholder implementation
    }

    private boolean isTeacherAuthorizedForGrade(Teacher teacher, Long gradeId) {
        // TODO: Implement logic to check if teacher created the grade
        // This could involve checking the grade's teacher ID against the current teacher
        return true; // Placeholder implementation
    }
} 