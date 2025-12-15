package com.example.demo;

import com.example.demo.dto.GegenstandCreateDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
public class GegenstandController {

    @Autowired
    private GegenstandService service;

    @GetMapping("/gegenstaende")
    public List<Gegenstand> getAlleGegenstaende() {
        return service.getAll();
    }

    @PostMapping("/gegenstaende")
    public Gegenstand createGegenstand(@Valid @RequestBody GegenstandCreateDto dto) {
        return service.saveFromDto(dto);
    }
}