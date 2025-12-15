package com.example.demo;

import com.example.demo.dto.GegenstandCreateDto;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

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


    @PostMapping
    public Gegenstand create(@Valid @RequestBody GegenstandCreateDto dto) {
        return service.create(dto);
    }


    @GetMapping("/{id}")
    public Gegenstand getById(@PathVariable Long id) {
        return service.getById(id);
    }


    @PutMapping("/{id}")
    public Gegenstand update(@PathVariable Long id, @Valid @RequestBody GegenstandCreateDto dto) {
        return service.update(id, dto);
    }


    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

}