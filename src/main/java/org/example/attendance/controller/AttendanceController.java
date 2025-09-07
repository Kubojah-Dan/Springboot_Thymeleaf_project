package org.example.attendance.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.example.attendance.entity.Attendance;
import org.example.attendance.entity.Lecture;
import org.example.attendance.entity.User;
import org.example.attendance.repository.AttendanceRepository;
import org.example.attendance.repository.LectureRepository;
import org.example.attendance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
public class AttendanceController {
    @Autowired
    private LectureRepository lectureRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/instructor/lectures/{lectureId}/qr")
    public @ResponseBody byte[] generateQRCode(@PathVariable Long lectureId) throws WriterException, IOException {
        Lecture lecture = lectureRepository.findById(lectureId).orElse(null);
        if (lecture == null) {
            return null;
        }
        String token = UUID.randomUUID().toString();
        lecture.setQrCodeToken(token);
        lectureRepository.save(lecture);

        String qrCodeData = "http://localhost:8080/attend?token=" + token;
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(qrCodeData, BarcodeFormat.QR_CODE, 200, 200);
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return pngOutputStream.toByteArray();
    }

    @GetMapping("/attend")
    public String markAttendance(@RequestParam("token") String token, Model model) {
        Lecture lecture = lectureRepository.findByQrCodeToken(token);
        if (lecture == null || lecture.getEndTime().isBefore(LocalDateTime.now())) {
            model.addAttribute("message", "Invalid or expired QR code.");
            return "error";
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User student = userRepository.findByUsername(username);
        if (student == null || !student.getRole().equals("STUDENT")) {
            model.addAttribute("message", "Only students can mark attendance.");
            return "error";
        }
        Attendance attendance = new Attendance();
        attendance.setStudent(student);
        attendance.setLecture(lecture);
        attendance.setTimestamp(LocalDateTime.now());
        attendanceRepository.save(attendance);
        return "success";
    }

    @GetMapping("/student/attendance/{username}")
    @ResponseBody
    public ResponseEntity<?> getAttendanceRecords(@PathVariable String username) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getName().equals(username) || !auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"))) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        List<Attendance> records = attendanceRepository.findAll().stream()
                .filter(a -> a.getStudent().getUsername().equals(username))
                .collect(Collectors.toList());
        List<AttendanceRecord> response = records.stream()
                .map(a -> new AttendanceRecord(a.getLecture().getId(), a.getTimestamp().toString(), a.getStudent().getUsername()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    private record AttendanceRecord(Long lectureId, String timestamp, String studentId) {}
}



