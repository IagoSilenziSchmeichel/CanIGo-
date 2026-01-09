package com.example.demo;

import com.example.demo.dto.GegenstandCreateDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/gegenstaende")
public class GegenstandController {

    private final GegenstandService service;

    public GegenstandController(GegenstandService service) {
        this.service = service;
    }

    @GetMapping
    public List<Gegenstand> getAlleGegenstaende() {
        Long userId = currentUserIdOrThrow();
        return service.getAllForUser(userId);
    }

    @GetMapping("/{id}")
    public Gegenstand getEinen(@PathVariable Long id) {
        Long userId = currentUserIdOrThrow();
        return service.getByIdForUser(userId, id);
    }

    @PostMapping
    public ResponseEntity<Gegenstand> create(@Valid @RequestBody GegenstandCreateDto dto) {
        Long userId = currentUserIdOrThrow();
        Gegenstand created = service.createForUser(userId, dto);

        return ResponseEntity
                .created(URI.create("/gegenstaende/" + created.getId()))
                .body(created);
    }

    @PutMapping("/{id}")
    public Gegenstand update(@PathVariable Long id, @Valid @RequestBody GegenstandCreateDto dto) {
        Long userId = currentUserIdOrThrow();
        return service.updateForUser(userId, id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Long userId = currentUserIdOrThrow();
        service.deleteForUser(userId, id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Holt die User-ID des eingeloggten Users.
     * Wir nehmen Authentication.getName() (Email/Username) und mappen auf die interne User-ID.
     */
    private Long currentUserIdOrThrow() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();

        if (a == null
                || !a.isAuthenticated()
                || a instanceof AnonymousAuthenticationToken
                || a.getName() == null
                || a.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nicht eingeloggt");
        }

        return service.resolveUserIdByEmail(a.getName());
    }
}