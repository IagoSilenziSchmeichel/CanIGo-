package com.example.demo.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Profile("!test")
@Configuration
public class SecurityConfig {

    @Value("${app.frontend.success-url}")
    private String successUrl;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // SPA + API => wir machen es erstmal unkompliziert:
                .csrf(csrf -> csrf.disable())

                .cors(cors -> cors.configurationSource(req -> {
                    CorsConfiguration cfg = new CorsConfiguration();
                    cfg.setAllowedOrigins(List.of(
                            "http://localhost:5173",
                            "https://canigo-frontend.onrender.com"
                    ));
                    cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
                    cfg.setAllowedHeaders(List.of("*"));
                    cfg.setAllowCredentials(true); // wichtig für Session-Cookie
                    return cfg;
                }))

                .authorizeHttpRequests(auth -> auth
                        // public:
                        .requestMatchers("/", "/error").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/oauth2/**", "/login/**").permitAll()

                        // API: nur wenn eingeloggt
                        .requestMatchers("/api/**").authenticated()

                        // deine bisherigen endpoints: wenn du sie schützen willst:
                        .requestMatchers(HttpMethod.GET, "/gegenstaende/**").authenticated()
                        .requestMatchers("/notifications/**").authenticated()

                        .anyRequest().permitAll()
                )

                .oauth2Login(oauth -> oauth
                        // Nach erfolgreichem Login -> ins Frontend zurück
                        .defaultSuccessUrl(successUrl, true)
                )

                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessUrl(successUrl)
                );

        return http.build();
    }
}