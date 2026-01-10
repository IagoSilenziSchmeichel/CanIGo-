package com.example.demo.auth;

import com.example.demo.security.JwtService;
import com.example.demo.user.AppUser;
import com.example.demo.user.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AppUserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(AppUserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    public AuthDtos.AuthResponse register(RegisterRequest req) {
        String email = normalizeEmail(req.getEmail());
        String password = req.getPassword();
        String name = normalizeName(req.getName());

        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email must not be empty");
        if (password == null || password.isBlank()) throw new IllegalArgumentException("Password must not be empty");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name must not be empty");

        if (users.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already in use");
        }

        AppUser u = new AppUser();
        u.setEmail(email);
        u.setName(name);
        u.setPasswordHash(encoder.encode(password));

        u = users.save(u);

        String token = jwt.createToken(u.getId());
        return new AuthDtos.AuthResponse(token, u.getId(), u.getEmail(), u.getName());
    }

    public AuthDtos.AuthResponse login(LoginRequest req) {
        String email = normalizeEmail(req.getEmail());
        String password = req.getPassword();

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        AppUser u = users.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!encoder.matches(password, u.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = jwt.createToken(u.getId());
        return new AuthDtos.AuthResponse(token, u.getId(), u.getEmail(), u.getName());
    }

    private String normalizeEmail(String email) {
        if (email == null) return null;
        return email.trim().toLowerCase();
    }

    private String normalizeName(String name) {
        if (name == null) return null;
        return name.trim();
    }
}