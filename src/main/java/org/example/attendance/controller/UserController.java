package org.example.attendance.controller;

import org.example.attendance.entity.User;
import org.example.attendance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/user/info")
    public ResponseEntity<?> getUserInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.ok(new UserInfo(null, null)); // Return null user for unauthenticated
        }
        String username = auth.getName();
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return ResponseEntity.ok(new UserInfo(null, null));
        }
        return ResponseEntity.ok(new UserInfo(user.getUsername(), user.getRole()));
    }

    private record UserInfo(String username, String role) {}
}


