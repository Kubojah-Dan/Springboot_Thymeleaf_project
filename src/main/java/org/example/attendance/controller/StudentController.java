package org.example.attendance.controller;

import org.example.attendance.entity.Course;
import org.example.attendance.entity.Enrollment;
import org.example.attendance.repository.CourseRepository;
import org.example.attendance.repository.EnrollmentRepository;
import org.example.attendance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/student")
public class StudentController {
    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/courses")
    public String listCourses(Model model) {
        model.addAttribute("courses", courseRepository.findAll());
        return "student_courses";
    }

    @PostMapping("/courses/{courseId}/enroll")
    public String enroll(@PathVariable Long courseId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        org.example.attendance.entity.User student = userRepository.findByUsername(username);
        if (student == null || !student.getRole().equals("STUDENT")) {
            model.addAttribute("message", "Only students can enroll in courses.");
            return "error";
        }
        if (enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), courseId)) {
            model.addAttribute("message", "You are already enrolled in this course.");
            return "error";
        }
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(new Course());
        enrollment.getCourse().setId(courseId);
        enrollmentRepository.save(enrollment);
        return "redirect:/student/my-courses";
    }

    @GetMapping("/my-courses")
    public String myCourses(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        org.example.attendance.entity.User student = userRepository.findByUsername(username);
        model.addAttribute("enrollments", enrollmentRepository.findAll().stream()
                .filter(e -> e.getStudent().getId().equals(student.getId()))
                .toList());
        return "my_courses";
    }
}
