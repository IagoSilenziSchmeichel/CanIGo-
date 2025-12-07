package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
public class GegenstandController {

    @Autowired
    private GegenstandRepository repo; // Verbindung zur Datenbank

    // GET: Alle holen (aus der DB)
    @GetMapping("/gegenstaende")
    public List<Gegenstand> getAlleGegenstaende() {
        return repo.findAll();
    }

    // POST: Neuen erstellen (in die DB speichern)
    @PostMapping("/gegenstaende")
    public Gegenstand createGegenstand(@RequestBody Gegenstand g) {
        return repo.save(g);
    }
}