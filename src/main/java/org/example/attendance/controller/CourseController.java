package org.example.attendance.controller;

import org.example.attendance.entity.Course;
import org.example.attendance.entity.User;
import org.example.attendance.repository.CourseRepository;
import org.example.attendance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/instructor")
public class CourseController {
    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/courses")
    public ResponseEntity<?> listCourses() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User instructor = userRepository.findByUsername(username);
        if (instructor == null || !instructor.getRole().equals("INSTRUCTOR")) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        List<Course> courses = courseRepository.findAll().stream()
                .filter(course -> course.getInstructor() != null && course.getInstructor().getId().equals(instructor.getId()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(courses);
    }

    @PostMapping("/courses/new")
    public ResponseEntity<?> createCourse(@RequestBody Course course) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User instructor = userRepository.findByUsername(username);
        if (instructor == null || !instructor.getRole().equals("INSTRUCTOR")) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        if (course.getName() == null || course.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Course name is required");
        }
        course.setInstructor(instructor);
        Course savedCourse = courseRepository.save(course);
        return ResponseEntity.ok(savedCourse);
    }
}
