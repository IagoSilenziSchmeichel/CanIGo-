package com.example.demo;

import com.example.demo.dto.GegenstandCreateDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/gegenstaende")
public class GegenstandController {

    private final GegenstandService service;

    public GegenstandController(GegenstandService service) {
        this.service = service;
    }

    @GetMapping
    public List<Gegenstand> getAlleGegenstaende() {
        return service.getAll();
    }


    @GetMapping("/{id}")
    public Gegenstand getEinen(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<Gegenstand> create(@Valid @RequestBody GegenstandCreateDto dto) {
        Gegenstand created = service.create(dto);
        // Location Header: /gegenstaende/{id}
        return ResponseEntity
                .created(URI.create("/gegenstaende/" + created.getId()))
                .body(created);
    }

    @PutMapping("/{id}")
    public Gegenstand update(@PathVariable Long id, @Valid @RequestBody GegenstandCreateDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
