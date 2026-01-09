package com.example.demo.security;

import com.example.demo.user.AppUserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter implements Filter {

    private final JwtService jwtService;
    private final AppUserRepository users;

    public JwtAuthFilter(JwtService jwtService, AppUserRepository users) {
        this.jwtService = jwtService;
        this.users = users;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        String auth = req.getHeader("Authorization");

        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            try {
                Claims claims = jwtService.parse(token).getBody();
                Long userId = Long.valueOf(claims.getSubject());

                // user exist check (optional, aber gut)
                users.findById(userId).ifPresent(u -> {
                    var authentication = new UsernamePasswordAuthenticationToken(
                            u.getId(), // principal = userId
                            null,
                            List.of(new SimpleGrantedAuthority("USER"))
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                });

            } catch (Exception ignored) {
                // invalid token -> einfach nicht authentifiziert
                SecurityContextHolder.clearContext();
            }
        }

        chain.doFilter(request, response);
    }
}