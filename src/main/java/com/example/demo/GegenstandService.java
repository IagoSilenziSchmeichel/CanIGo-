package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class GegenstandService {

    @Autowired
    private GegenstandRepository repo;

    public Gegenstand save(Gegenstand g) {
        return repo.save(g);
    }

    public List<Gegenstand> getAll() {
        // repo.findAll() gibt ein Iterable zurück, wir wandeln es in eine Liste um
        // (oder wir ändern den Rückgabetyp, aber so ist es wie beim Prof)
        return (List<Gegenstand>) repo.findAll();
    }
}