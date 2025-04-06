package com.ednevnik.dnevnik.controller;

import com.ednevnik.dnevnik.model.Class;
import com.ednevnik.dnevnik.model.School;
import com.ednevnik.dnevnik.model.Student;
import com.ednevnik.dnevnik.model.Teacher;
import com.ednevnik.dnevnik.repository.ClassRepository;
import com.ednevnik.dnevnik.repository.SchoolRepository;
import com.ednevnik.dnevnik.repository.StudentRepository;
import com.ednevnik.dnevnik.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/classes")
@PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
@RequiredArgsConstructor
public class ClassController {

    private final ClassRepository classRepository;
    private final SchoolRepository schoolRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;

    @GetMapping
    public String listClasses(Model model) {
        model.addAttribute("classes", classRepository.findAll());
        model.addAttribute("schools", schoolRepository.findAll());
        model.addAttribute("teachers", teacherRepository.findAll());
        model.addAttribute("newClass", new Class());
        return "classes/list";
    }

    @GetMapping("/{id}")
    public String showClass(@PathVariable Long id, Model model) {
        Class schoolClass = classRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid class ID"));
        
        List<Student> availableStudents = studentRepository.findAll().stream()
            .filter(student -> student.getStudentClass() == null)
            .toList();

        model.addAttribute("schoolClass", schoolClass);
        model.addAttribute("availableStudents", availableStudents);
        return "classes/details";
    }

    @PostMapping
    public String createClass(@ModelAttribute Class schoolClass, RedirectAttributes redirectAttributes) {
        try {
            schoolClass.setCreatedTimestamp(System.currentTimeMillis());
            classRepository.save(schoolClass);
            redirectAttributes.addFlashAttribute("success", "Class created successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create class: " + e.getMessage());
        }
        return "redirect:/classes";
    }

    @PostMapping("/{id}/delete")
    public String deleteClass(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            classRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Class deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete class: " + e.getMessage());
        }
        return "redirect:/classes";
    }

    @PostMapping("/{id}/update")
    public String updateClass(@PathVariable Long id, @ModelAttribute Class schoolClass, RedirectAttributes redirectAttributes) {
        try {
            Class existingClass = classRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid class ID"));
            
            existingClass.setName(schoolClass.getName());
            existingClass.setGrade(schoolClass.getGrade());
            existingClass.setSchool(schoolClass.getSchool());
            existingClass.setClassTeacher(schoolClass.getClassTeacher());
            
            classRepository.save(existingClass);
            redirectAttributes.addFlashAttribute("success", "Class updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update class: " + e.getMessage());
        }
        return "redirect:/classes";
    }

    @PostMapping("/{classId}/students/{studentId}/assign")
    public String assignStudent(@PathVariable Long classId, @PathVariable Long studentId, RedirectAttributes redirectAttributes) {
        try {
            Class schoolClass = classRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid class ID"));
            Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid student ID"));

            student.setStudentClass(schoolClass);
            studentRepository.save(student);
            redirectAttributes.addFlashAttribute("success", "Student assigned successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to assign student: " + e.getMessage());
        }
        return "redirect:/classes/" + classId;
    }

    @PostMapping("/{classId}/students/{studentId}/remove")
    public String removeStudent(@PathVariable Long classId, @PathVariable Long studentId, RedirectAttributes redirectAttributes) {
        try {
            Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid student ID"));

            student.setStudentClass(null);
            studentRepository.save(student);
            redirectAttributes.addFlashAttribute("success", "Student removed successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to remove student: " + e.getMessage());
        }
        return "redirect:/classes/" + classId;
    }
}