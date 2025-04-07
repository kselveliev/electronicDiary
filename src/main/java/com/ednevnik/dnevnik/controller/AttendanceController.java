package com.ednevnik.dnevnik.controller;

import com.ednevnik.dnevnik.model.*;
import com.ednevnik.dnevnik.repository.*;
import com.ednevnik.dnevnik.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final SubjectRepository subjectRepository;
    private final ClassRepository classRepository;
    private final AttendanceRepository attendanceRepository;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public String showAttendanceForm(Model model, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Teacher teacher = (Teacher) userDetails.getUser();

        // Get teacher's subjects and classes
        List<Subject> subjects = subjectRepository.findAll(); // TODO: Filter by teacher's subjects
        List<com.ednevnik.dnevnik.model.Class> classes = classRepository.findBySchoolId(teacher.getSchool().getId());

        model.addAttribute("subjects", subjects);
        model.addAttribute("classes", classes);
        model.addAttribute("teacher", teacher);

        return "attendance/form";
    }

    @GetMapping("/take")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public String takeAttendance(@RequestParam Long classId,
                               @RequestParam Long subjectId,
                               @RequestParam Integer lessonNumber,
                               Model model,
                               Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Teacher teacher = (Teacher) userDetails.getUser();

        com.ednevnik.dnevnik.model.Class studentClass = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        // Get all students in the class
        List<Student> students = studentRepository.findByStudentClassId(studentClass.getId());

        model.addAttribute("students", students);
        model.addAttribute("class", studentClass);
        model.addAttribute("subject", subject);
        model.addAttribute("lessonNumber", lessonNumber);
        model.addAttribute("teacher", teacher);

        return "attendance/take";
    }

    @PostMapping("/save")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public String saveAttendance(@RequestParam Long classId,
                               @RequestParam Long subjectId,
                               @RequestParam Integer lessonNumber,
                               @RequestParam(required = false) List<Long> studentIds,
                               @RequestParam(required = false) List<String> notes,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Teacher teacher = (Teacher) userDetails.getUser();

            com.ednevnik.dnevnik.model.Class studentClass = classRepository.findById(classId)
                    .orElseThrow(() -> new RuntimeException("Class not found"));
            Subject subject = subjectRepository.findById(subjectId)
                    .orElseThrow(() -> new RuntimeException("Subject not found"));

            // Only process if there are absent students
            if (studentIds != null && !studentIds.isEmpty()) {
                for (int i = 0; i < studentIds.size(); i++) {
                    Student student = studentRepository.findById(studentIds.get(i))
                            .orElseThrow(() -> new RuntimeException("Student not found"));

                    Attendance attendance = new Attendance();
                    attendance.setStudent(student);
                    attendance.setTeacher(teacher);
                    attendance.setSubject(subject);
                    attendance.setStudentClass(studentClass);
                    attendance.setLessonNumber(lessonNumber);
                    attendance.setPresent(false); // These are absent students
                    
                    if (notes != null && i < notes.size()) {
                        attendance.setNote(notes.get(i));
                    }

                    attendanceRepository.save(attendance);
                }
            }

            redirectAttributes.addFlashAttribute("success", "Attendance saved successfully");
            return "redirect:/attendance";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error saving attendance: " + e.getMessage());
            return "redirect:/attendance";
        }
    }

    @GetMapping("/my-attendance")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public String showMyAttendance(Model model, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Student student = (Student) userDetails.getUser();

        List<Attendance> attendances = attendanceRepository.findByStudentOrderByCreatedTimestampDesc(student);
        
        model.addAttribute("student", student);
        model.addAttribute("attendances", attendances);
        
        return "attendance/student-view";
    }

    @GetMapping("/children-attendance")
    @PreAuthorize("hasRole('ROLE_PARENT')")
    public String showChildrenAttendance(Model model, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Parent parent = (Parent) userDetails.getUser();

        List<Student> children = new ArrayList<>(parent.getChildren());
        List<Attendance> attendances = attendanceRepository.findByStudentInOrderByCreatedTimestampDesc(children);
        
        model.addAttribute("children", children);
        model.addAttribute("attendances", attendances);
        
        return "attendance/parent-view";
    }

    @GetMapping("/class-view")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public String showClassAttendance(@RequestParam(required = false) Long classId,
                                    @RequestParam(required = false) Long subjectId,
                                    Model model,
                                    Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Teacher teacher = (Teacher) userDetails.getUser();

        List<com.ednevnik.dnevnik.model.Class> classes = classRepository.findBySchoolId(teacher.getSchool().getId());
        List<Subject> subjects = subjectRepository.findAll(); // TODO: Filter by teacher's subjects

        model.addAttribute("classes", classes);
        model.addAttribute("subjects", subjects);

        if (classId != null) {
            com.ednevnik.dnevnik.model.Class selectedClass = classRepository.findById(classId)
                    .orElseThrow(() -> new RuntimeException("Class not found"));
            model.addAttribute("selectedClass", selectedClass);

            List<Student> students = studentRepository.findByStudentClassId(classId);
            model.addAttribute("students", students);

            if (subjectId != null) {
                Subject selectedSubject = subjectRepository.findById(subjectId)
                        .orElseThrow(() -> new RuntimeException("Subject not found"));
                model.addAttribute("selectedSubject", selectedSubject);

                // Get attendance records for the selected class and subject
                List<Attendance> attendances = attendanceRepository.findByStudentClassIdAndSubjectIdOrderByCreatedTimestampDesc(classId, subjectId);
                model.addAttribute("attendances", attendances);
            }
        }

        return "attendance/class-view";
    }
} 