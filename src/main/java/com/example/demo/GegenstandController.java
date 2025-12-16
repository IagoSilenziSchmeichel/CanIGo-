package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
public class GegenstandController {

    @Autowired
    private GegenstandService service; // <-- Hier nutzen wir jetzt den Service!

    @GetMapping("/gegenstaende")
    public List<Gegenstand> getAlleGegenstaende() {
        return service.getAll();
    }

    @PostMapping("/gegenstaende")
    public Gegenstand createGegenstand(@RequestBody Gegenstand g) {
        return service.save(g);
    }
}