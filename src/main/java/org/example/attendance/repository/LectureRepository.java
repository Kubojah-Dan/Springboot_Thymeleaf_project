package org.example.attendance.repository;

import org.example.attendance.entity.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LectureRepository extends JpaRepository<Lecture, Long> {
    Lecture findByQrCodeToken(String token);
}

