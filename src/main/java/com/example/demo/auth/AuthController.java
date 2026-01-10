package com.example.demo.auth;

import com.example.demo.security.JwtService;
import com.example.demo.user.AppUser;
import com.example.demo.user.AppUserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import static com.example.demo.auth.AuthDtos.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AppUserRepository users;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthController(AppUserRepository users, JwtService jwtService) {
        this.users = users;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        if (users.existsByEmail(req.email())) {
            return ResponseEntity.badRequest().body("Email already in use");
        }

        AppUser u = new AppUser();
        u.setName(req.name().trim());
        u.setEmail(req.email().trim().toLowerCase());
        u.setPasswordHash(encoder.encode(req.password()));

        u = users.save(u);

        String token = jwtService.createToken(u.getId());
        return ResponseEntity.ok(new AuthResponse(token, u.getId(), u.getEmail(), u.getName()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        var uOpt = users.findByEmail(req.email().trim().toLowerCase());
        if (uOpt.isEmpty()) return ResponseEntity.status(401).body("Invalid credentials");

        AppUser u = uOpt.get();
        if (!encoder.matches(req.password(), u.getPasswordHash())) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        String token = jwtService.createToken(u.getId());
        return ResponseEntity.ok(new AuthResponse(token, u.getId(), u.getEmail(), u.getName()));
    }
}