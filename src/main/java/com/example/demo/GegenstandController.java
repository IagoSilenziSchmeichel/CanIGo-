package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class GegenstandController {

    // 1. Einfache URL, genau wie im "/todos"-Beispiel
    @GetMapping("/gegenstaende")
    public List<Gegenstand> getAlleGegenstaende() {

        // 2. Kurze und moderne Listenerstellung mit List.of(), genau wie im Beispiel
        return List.of(
                new Gegenstand(1L, "Alte Winterreifen", "Keller", "Wegwerfen"),
                new Gegenstand(2L, "Comic-Sammlung", "Dachboden", "Behalten"),
                new Gegenstand(3L, "Kaputte Kaffeemaschine", "Küche", "Wegwerfen"),
                new Gegenstand(4L, "Omas altes Porzellan", "Keller", "Verkaufen")
        );
    }
}