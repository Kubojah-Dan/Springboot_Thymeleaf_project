package org.example.attendance.controller;

import org.example.attendance.entity.Course;
import org.example.attendance.entity.Lecture;
import org.example.attendance.entity.User;
import org.example.attendance.repository.CourseRepository;
import org.example.attendance.repository.LectureRepository;
import org.example.attendance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/instructor")
public class InstructorController {
    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LectureRepository lectureRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<?> getInstructorDashboard() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User instructor = userRepository.findByUsername(username);
        if (instructor == null || !instructor.getRole().equals("INSTRUCTOR")) {
            return ResponseEntity.status(403).body("Unauthorized");
        }

        // Fetch instructor's courses
        List<Course> courses = courseRepository.findAll().stream()
                .filter(course -> course.getInstructor() != null && course.getInstructor().getId().equals(instructor.getId()))
                .collect(Collectors.toList());

        // Fetch all lectures for instructor's courses
        List<Lecture> lectures = lectureRepository.findAll().stream()
                .filter(lecture -> lecture.getCourse() != null && lecture.getCourse().getInstructor() != null
                        && lecture.getCourse().getInstructor().getId().equals(instructor.getId()))
                .collect(Collectors.toList());

        // Prepare response
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("instructor", instructor.getUsername());
        dashboard.put("courses", courses);
        dashboard.put("lectures", lectures);

        return ResponseEntity.ok(dashboard);
    }
}

