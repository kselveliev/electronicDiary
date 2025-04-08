package com.ednevnik.dnevnik.controller;

import com.ednevnik.dnevnik.model.*;
import com.ednevnik.dnevnik.repository.ClassAssignmentRepository;
import com.ednevnik.dnevnik.repository.ClassRepository;
import com.ednevnik.dnevnik.repository.SubjectRepository;
import com.ednevnik.dnevnik.repository.TeacherRepository;
import com.ednevnik.dnevnik.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/teacher-assignments")
@PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
@RequiredArgsConstructor
public class TeacherAssignmentController {

    private final TeacherRepository teacherRepository;
    private final SubjectRepository subjectRepository;
    private final ClassRepository classRepository;
    private final ClassAssignmentRepository classAssignmentRepository;

    @GetMapping
    public String showTeacherAssignments(Model model, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userDetails.getUser();

        // For admin, show all teachers
        if (user.getRole() == UserRole.ROLE_ADMIN) {
            model.addAttribute("teachers", teacherRepository.findAll());
        }
        // For director, show only teachers from their school
        else if (user.getRole() == UserRole.ROLE_DIRECTOR) {
            Director director = (Director) user;
            model.addAttribute("teachers", teacherRepository.findBySchoolId(director.getSchool().getId()));
        }

        return "teacher-assignments/list";
    }

    @GetMapping("/{teacherId}")
    public String showTeacherDetails(@PathVariable Long teacherId, Model model, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userDetails.getUser();
        
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid teacher ID"));

        // Check if director has access to this teacher
        if (user.getRole() == UserRole.ROLE_DIRECTOR) {
            Director director = (Director) user;
            if (!director.getSchool().getId().equals(teacher.getSchool().getId())) {
                throw new IllegalArgumentException("Access denied: Teacher not from your school");
            }
        }
        
        model.addAttribute("teacher", teacher);
        model.addAttribute("subjects", subjectRepository.findAll());
        model.addAttribute("classes", teacher.getSchool() != null ? 
            classRepository.findBySchoolId(teacher.getSchool().getId()) : 
            List.of());
        model.addAttribute("assignments", classAssignmentRepository.findByTeacherId(teacherId));
        return "teacher-assignments/details";
    }

    @PostMapping("/{teacherId}/assign")
    public String assignSubjectAndClass(
            @PathVariable Long teacherId,
            @RequestParam Long subjectId,
            @RequestParam Long classId,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User user = userDetails.getUser();
            
            Teacher teacher = teacherRepository.findById(teacherId)
                    .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));

            // Check if director has access to this teacher
            if (user.getRole() == UserRole.ROLE_DIRECTOR) {
                Director director = (Director) user;
                if (!director.getSchool().getId().equals(teacher.getSchool().getId())) {
                    throw new IllegalArgumentException("Access denied: Teacher not from your school");
                }
            }

            Subject subject = subjectRepository.findById(subjectId)
                    .orElseThrow(() -> new IllegalArgumentException("Subject not found"));
            com.ednevnik.dnevnik.model.Class schoolClass = classRepository.findById(classId)
                    .orElseThrow(() -> new IllegalArgumentException("Class not found"));

            // Verify the class belongs to the same school as the teacher
            if (!schoolClass.getSchool().getId().equals(teacher.getSchool().getId())) {
                throw new IllegalArgumentException("Class must be from the same school as the teacher");
            }

            ClassAssignment assignment = new ClassAssignment();
            assignment.setTeacher(teacher);
            assignment.setSubject(subject);
            assignment.setSchoolClass(schoolClass);
            assignment.setCreatedTimestamp(System.currentTimeMillis());

            classAssignmentRepository.save(assignment);
            redirectAttributes.addFlashAttribute("success", "Subject and class successfully assigned to teacher");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to assign: " + e.getMessage());
        }
        return "redirect:/teacher-assignments/" + teacherId;
    }

    @PostMapping("/{teacherId}/remove")
    public String removeAssignment(
            @PathVariable Long teacherId,
            @RequestParam Long assignmentId,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User user = userDetails.getUser();
            
            Teacher teacher = teacherRepository.findById(teacherId)
                    .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));

            // Check if director has access to this teacher
            if (user.getRole() == UserRole.ROLE_DIRECTOR) {
                Director director = (Director) user;
                if (!director.getSchool().getId().equals(teacher.getSchool().getId())) {
                    throw new IllegalArgumentException("Access denied: Teacher not from your school");
                }
            }

            classAssignmentRepository.deleteById(assignmentId);
            redirectAttributes.addFlashAttribute("success", "Assignment successfully removed");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to remove assignment: " + e.getMessage());
        }
        return "redirect:/teacher-assignments/" + teacherId;
    }
} 