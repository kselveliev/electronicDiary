package com.ednevnik.dnevnik.controller;

import com.ednevnik.dnevnik.model.School;
import com.ednevnik.dnevnik.model.User;
import com.ednevnik.dnevnik.model.UserRole;
import com.ednevnik.dnevnik.model.Teacher;
import com.ednevnik.dnevnik.model.Student;
import com.ednevnik.dnevnik.repository.SchoolRepository;
import com.ednevnik.dnevnik.repository.UserRepository;
import com.ednevnik.dnevnik.security.UserDetailsImpl;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/schools")
public class SchoolEnrollmentController {

    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;

    public SchoolEnrollmentController(SchoolRepository schoolRepository, UserRepository userRepository) {
        this.schoolRepository = schoolRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/{id}/enrollment")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public String showEnrollmentPage(@PathVariable Long id, Model model) {
        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("School not found"));

        // Check if the current user is the director of this school
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userDetails.getUser();

        if (currentUser.getRole() == UserRole.ROLE_DIRECTOR && 
            (school.getDirector() == null || !school.getDirector().getId().equals(currentUser.getId()))) {
            throw new RuntimeException("Access denied");
        }

        // Get teachers and students for this school
        List<User> schoolTeachers = userRepository.findBySchoolAndRole(school, UserRole.ROLE_TEACHER);
        List<User> schoolStudents = userRepository.findBySchoolAndRole(school, UserRole.ROLE_STUDENT);

        // Get available teachers and students (not assigned to any school)
        List<User> availableTeachers = userRepository.findByRoleAndSchoolIsNull(UserRole.ROLE_TEACHER);
        List<User> availableStudents = userRepository.findByRoleAndSchoolIsNull(UserRole.ROLE_STUDENT);

        model.addAttribute("school", school);
        model.addAttribute("schoolTeachers", schoolTeachers);
        model.addAttribute("schoolStudents", schoolStudents);
        model.addAttribute("availableTeachers", availableTeachers);
        model.addAttribute("availableStudents", availableStudents);

        return "schools/enrollment";
    }

    @PostMapping("/{schoolId}/assign-teacher")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public String assignTeacher(@PathVariable Long schoolId,
                              @RequestParam Long teacherId,
                              RedirectAttributes redirectAttributes) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found"));
        User user = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        if (user.getRole() != UserRole.ROLE_TEACHER) {
            throw new RuntimeException("Selected user is not a teacher");
        }

        Teacher teacher = (Teacher) user;
        teacher.setSchool(school);
        userRepository.save(teacher);

        redirectAttributes.addFlashAttribute("success", 
            String.format("Teacher %s %s successfully assigned to %s", 
                teacher.getFirstName(), teacher.getLastName(), school.getName()));
        return "redirect:/schools/" + schoolId + "/enrollment";
    }

    @PostMapping("/{schoolId}/assign-student")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public String assignStudent(@PathVariable Long schoolId,
                              @RequestParam Long studentId,
                              RedirectAttributes redirectAttributes) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found"));
        User user = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (user.getRole() != UserRole.ROLE_STUDENT) {
            throw new RuntimeException("Selected user is not a student");
        }

        Student student = (Student) user;
        student.setSchool(school);
        userRepository.save(student);

        redirectAttributes.addFlashAttribute("success", 
            String.format("Student %s %s successfully assigned to %s", 
                student.getFirstName(), student.getLastName(), school.getName()));
        return "redirect:/schools/" + schoolId + "/enrollment";
    }

    @PostMapping("/{schoolId}/remove-teacher")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public String removeTeacher(@PathVariable Long schoolId,
                              @RequestParam Long teacherId,
                              RedirectAttributes redirectAttributes) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found"));
        User user = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        if (user.getRole() != UserRole.ROLE_TEACHER) {
            throw new RuntimeException("Selected user is not a teacher");
        }

        Teacher teacher = (Teacher) user;
        teacher.setSchool(null);
        userRepository.save(teacher);

        redirectAttributes.addFlashAttribute("success", 
            String.format("Teacher %s %s successfully removed from %s", 
                teacher.getFirstName(), teacher.getLastName(), school.getName()));
        return "redirect:/schools/" + schoolId + "/enrollment";
    }

    @PostMapping("/{schoolId}/remove-student")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public String removeStudent(@PathVariable Long schoolId,
                              @RequestParam Long studentId,
                              RedirectAttributes redirectAttributes) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found"));
        User user = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (user.getRole() != UserRole.ROLE_STUDENT) {
            throw new RuntimeException("Selected user is not a student");
        }

        Student student = (Student) user;
        student.setSchool(null);
        userRepository.save(student);

        redirectAttributes.addFlashAttribute("success", 
            String.format("Student %s %s successfully removed from %s", 
                student.getFirstName(), student.getLastName(), school.getName()));
        return "redirect:/schools/" + schoolId + "/enrollment";
    }
} 