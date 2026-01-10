package com.example.demo.auth;

import com.example.demo.user.AppUser;
import com.example.demo.user.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AppUserRepository users;
    private final PasswordEncoder encoder;
    private final com.example.demo.security.JwtService jwt;

    public AuthService(AppUserRepository users, PasswordEncoder encoder, com.example.demo.security.JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    public String register(String email, String password) {
        String normEmail = email.trim().toLowerCase();

        if (users.findByEmailIgnoreCase(normEmail).isPresent()) {
            throw new RuntimeException("Email existiert schon");
        }

        AppUser u = new AppUser();
        u.setEmail(normEmail);
        u.setPasswordHash(encoder.encode(password));
        u = users.save(u);

        // ✅ Token bekommt USER-ID (Long)
        return jwt.createToken(u.getId());
    }

    public String login(String email, String password) {
        AppUser u = users.findByEmailIgnoreCase(email.trim().toLowerCase())
                .orElseThrow(() -> new RuntimeException("User nicht gefunden"));

        if (!encoder.matches(password, u.getPasswordHash())) {
            throw new RuntimeException("Passwort falsch");
        }

        // ✅ Token bekommt USER-ID (Long)
        return jwt.createToken(u.getId());
    }
}