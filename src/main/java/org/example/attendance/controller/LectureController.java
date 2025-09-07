package org.example.attendance.controller;

import org.example.attendance.entity.Course;
import org.example.attendance.entity.Lecture;
import org.example.attendance.repository.CourseRepository;
import org.example.attendance.repository.LectureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/instructor")
public class LectureController {
    @Autowired
    private LectureRepository lectureRepository;

    @Autowired
    private CourseRepository courseRepository;

    @GetMapping("/courses/{courseId}/lectures")
    public ResponseEntity<?> listLectures(@PathVariable Long courseId) {
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) {
            return ResponseEntity.status(404).body("Course not found");
        }
        List<Lecture> lectures = lectureRepository.findAll().stream()
                .filter(lecture -> lecture.getCourse() != null && lecture.getCourse().getId().equals(courseId))
                .collect(Collectors.toList());
        return ResponseEntity.ok(lectures);
    }

    @PostMapping("/courses/{courseId}/lectures/new")
    public ResponseEntity<?> createLecture(@PathVariable Long courseId, @RequestBody Lecture lecture) {
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) {
            return ResponseEntity.status(404).body("Course not found");
        }
        if (lecture.getStartTime() == null || lecture.getEndTime() == null) {
            return ResponseEntity.badRequest().body("Start and end times are required");
        }
        lecture.setCourse(course);
        lecture.setDate(lecture.getStartTime().toLocalDate());
        lecture.setAttendanceToken(UUID.randomUUID().toString());
        Lecture savedLecture = lectureRepository.save(lecture);
        return ResponseEntity.ok(savedLecture);
    }
}



