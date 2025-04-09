package com.ednevnik.dnevnik.controller;

import com.ednevnik.dnevnik.model.*;
import com.ednevnik.dnevnik.repository.*;
import com.ednevnik.dnevnik.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.time.Instant;
import java.time.ZoneId;

@Controller
public class StatisticsController {
    private static final Logger logger = LoggerFactory.getLogger(StatisticsController.class);

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private SchoolRepository schoolRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public String showStatistics(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long subjectId,
            Authentication authentication,
            Model model) {

        logger.info("Accessing statistics page. User: {}, SchoolId: {}, SubjectId: {}", 
            authentication.getName(), schoolId, subjectId);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User currentUser = userDetails.getUser();

        logger.info("User role: {}", currentUser.getRole());

        // Add schools list and isAdmin flag for UI control
        if (currentUser.getRole() == UserRole.ROLE_ADMIN) {
            model.addAttribute("schools", schoolRepository.findAll());
            model.addAttribute("isAdmin", true);
        } else {
            model.addAttribute("isAdmin", false);
        }

        // Get all subjects for the dropdown
        List<Subject> allSubjects = subjectRepository.findAll();
        model.addAttribute("subjects", allSubjects);
        
        // Initialize maps for statistics
        Map<String, Map<Integer, Long>> gradeDistribution = new HashMap<>();
        Map<String, Map<String, Long>> attendanceDistribution = new HashMap<>();

        // Get subjects for statistics (filtered if subjectId is provided)
        List<Subject> subjectsForStats = subjectId != null 
            ? allSubjects.stream()
                .filter(s -> s.getId().equals(subjectId))
                .collect(Collectors.toList())
            : allSubjects;
        logger.info("Processing statistics for {} subjects", subjectsForStats.size());

        // Calculate statistics for each subject
        for (Subject subject : subjectsForStats) {
            List<Grade> grades;
            List<Attendance> attendances;

            if (currentUser.getRole() == UserRole.ROLE_DIRECTOR) {
                // For directors, only show their school's data
                grades = gradeRepository.findBySchoolAndSubject(currentUser.getSchool().getId(), subject.getId());
                attendances = attendanceRepository.findBySchoolAndSubject(currentUser.getSchool().getId(), subject.getId());
                logger.info("Director view - Subject: {}, Grades: {}, Attendances: {}", 
                    subject.getName(), grades.size(), attendances.size());
            } else {
                // For admins, filter by selected school if provided
                if (schoolId != null) {
                    grades = gradeRepository.findBySchoolAndSubject(schoolId, subject.getId());
                    attendances = attendanceRepository.findBySchoolAndSubject(schoolId, subject.getId());
                    logger.info("Admin view (filtered) - Subject: {}, School: {}, Grades: {}, Attendances: {}", 
                        subject.getName(), schoolId, grades.size(), attendances.size());
                } else {
                    // If no school selected, get all grades and attendances for the subject
                    grades = gradeRepository.findAll().stream()
                        .filter(g -> g.getSubject() != null && g.getSubject().getId().equals(subject.getId()))
                        .collect(Collectors.toList());
                    attendances = attendanceRepository.findAll().stream()
                        .filter(a -> a.getSubject() != null && a.getSubject().getId().equals(subject.getId()))
                        .collect(Collectors.toList());
                    logger.info("Admin view (all schools) - Subject: {}, Grades: {}, Attendances: {}", 
                        subject.getName(), grades.size(), attendances.size());
                }
            }

            // Only include subjects that have data
            if (!grades.isEmpty() || !attendances.isEmpty()) {
                // Calculate grade distribution
                Map<Integer, Long> gradeCounts = grades.stream()
                    .collect(Collectors.groupingBy(
                        Grade::getGrade,
                        Collectors.counting()
                    ));
                gradeDistribution.put(subject.getName(), gradeCounts);

                // Get the current date and 6 months ago
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime sixMonthsAgo = now.minusMonths(6);

                // Create a map with all months (including those with zero absences)
                Map<String, Long> absencesByMonth = new LinkedHashMap<>(); // Use LinkedHashMap to maintain order
                
                // Initialize all months with 0 absences
                for (int i = 0; i < 6; i++) {
                    LocalDateTime month = now.minusMonths(i);
                    String monthKey = month.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
                    absencesByMonth.put(monthKey, 0L);
                }

                // Calculate actual absences for each month
                attendances.stream()
                    .filter(a -> !a.getPresent()) // Only count absences
                    .filter(a -> {
                        LocalDateTime attendanceDate = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(a.getCreatedTimestamp()),
                            ZoneId.systemDefault()
                        );
                        return !attendanceDate.isBefore(sixMonthsAgo); // Only include last 6 months
                    })
                    .forEach(a -> {
                        LocalDateTime date = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(a.getCreatedTimestamp()),
                            ZoneId.systemDefault()
                        );
                        String monthKey = date.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
                        absencesByMonth.merge(monthKey, 1L, Long::sum);
                    });

                attendanceDistribution.put(subject.getName(), absencesByMonth);
            }
        }

        // Add data to model
        model.addAttribute("gradeDistribution", gradeDistribution);
        model.addAttribute("attendanceDistribution", attendanceDistribution);
        model.addAttribute("subjects", subjectsForStats);
        
        model.addAttribute("selectedSchoolId", schoolId);
        model.addAttribute("selectedSubjectId", subjectId);

        logger.info("Returning statistics view");
        return "statistics/index";
    }
} 