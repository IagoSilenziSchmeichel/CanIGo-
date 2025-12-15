package com.example.demo;

import com.example.demo.dto.GegenstandCreateDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GegenstandService {

    private final GegenstandRepository repo;

    public GegenstandService(GegenstandRepository repo) {
        this.repo = repo;
    }

    public Gegenstand saveFromDto(GegenstandCreateDto dto) {
        Gegenstand g = new Gegenstand();
        g.setName(dto.getName());
        g.setOrt(dto.getOrt());
        g.setStatus(dto.getStatus() == null ? "Neu" : dto.getStatus());
        return repo.save(g);
    }

    public List<Gegenstand> getAll() {
        return repo.findAll();
    }
}