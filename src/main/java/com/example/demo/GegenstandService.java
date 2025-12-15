package com.example.demo;

import com.example.demo.dto.GegenstandCreateDto;
import com.example.demo.error.NotFoundException;
import org.springframework.stereotype.Service;
import com.example.demo.error.NotFoundException;

import java.util.List;

@Service
public class GegenstandService {

    private final GegenstandRepository repo;

    public GegenstandService(GegenstandRepository repo) {
        this.repo = repo;
    }

    public Gegenstand create(GegenstandCreateDto dto) {
        Gegenstand g = new Gegenstand();
        applyDto(g, dto);
        return repo.save(g);
    }

    public Gegenstand getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Gegenstand nicht gefunden: " + id));
    }

    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new NotFoundException("Gegenstand nicht gefunden: " + id);
        }
        repo.deleteById(id);
    }
    public Gegenstand update(Long id, GegenstandCreateDto dto) {
        Gegenstand g = getById(id);
        applyDto(g, dto);
        return repo.save(g);
    }

    private void applyDto(Gegenstand g, GegenstandCreateDto dto){
        g.setName(dto.getName());
               g.setOrt(dto.getOrt());
               g.setWichtigkeit(dto.getWichtigkeit());
               g.setKategorie(dto.getKategorie());
               g.setLastUsed(dto.getLastUsed());
               g.setWegwerfAm(dto.getWegwerfAm());
                g.setKaufpreis(dto.getKaufpreis());
              g.setWunschVerkaufpreis(dto.getWunschVerkaufpreis());

    }
    public List<Gegenstand> getAll() {
        return repo.findAll();
    }
}