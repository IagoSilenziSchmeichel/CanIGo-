package com.example.demo.auth;

import com.example.demo.user.AppUser;
import com.example.demo.user.AppUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class MeController {

    private final AppUserRepository users;

    public MeController(AppUserRepository users) {
        this.users = users;
    }

    @GetMapping("/me")
    public Map<String, Object> me() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated() || a.getPrincipal() == null) {
            throw new RuntimeException("Not authenticated");
        }

        Long userId = (Long) a.getPrincipal();

        AppUser u = users.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return Map.of(
                "userId", u.getId(),
                "email", u.getEmail(),
                "name", u.getName()
        );
    }
}