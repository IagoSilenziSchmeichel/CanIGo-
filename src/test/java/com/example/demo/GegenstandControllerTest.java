package com.example.demo;

import com.example.demo.dto.GegenstandCreateDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GegenstandController.class)
class GegenstandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GegenstandService service;

    // --- TEST 1: Liste abrufen (leer) ---
    @Test
    void testGetAlle_Empty() throws Exception {
        when(service.getAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/gegenstaende"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
    }

    // --- TEST 2: Liste abrufen (mit Daten) ---
    @Test
    void testGetAlle_WithData() throws Exception {
        // Hier nutzen wir den neuen, großen Konstruktor
        var item = new Gegenstand(
                "Hammer",
                "Keller",
                Wichtigkeit.WICHTIG,
                GegenstandKategorie.HAUSHALT,
                null, null, null, null
        );
        // Da wir keine Setter für ID haben, simulieren wir das
        // (JPA setzt ID per Reflection, Mockito kann das auch, aber hier vereinfacht:)
        // Achtung: Wenn dein Gegenstand keine setId() hat, kann der Controller Test keine ID prüfen,
        // außer wir nutzen Reflection oder Mockito-Returns.
        // Für diesen Test reicht es, dass der Name stimmt.

        when(service.getAll()).thenReturn(List.of(item));

        mockMvc.perform(get("/gegenstaende"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Hammer"))
                .andExpect(jsonPath("$[0].wichtigkeit").value("WICHTIG"));
    }

    // --- TEST 3: Einzelnen Gegenstand holen ---
    @Test
    void testGetOne() throws Exception {
        var item = new Gegenstand("Buch", "Regal", Wichtigkeit.UNWICHTIG, GegenstandKategorie.SONSTIGES, null, null, null, null);
        when(service.getById(10L)).thenReturn(item);

        mockMvc.perform(get("/gegenstaende/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Buch"));
    }

    // --- TEST 4: Gegenstand erstellen (POST) ---
    @Test
    void testCreate_Success() throws Exception {
        // Das JSON muss jetzt auch die Enums enthalten!
        String jsonBody = """
            {
                "name": "Neu",
                "ort": "Hier",
                "wichtigkeit": "WICHTIG",
                "kategorie": "SONSTIGES"
            }
        """;

        var created = new Gegenstand("Neu", "Hier", Wichtigkeit.WICHTIG, GegenstandKategorie.SONSTIGES, null, null, null, null);
        // Wir nehmen an, dass die ID 1L ist (simuliert)

        // Da der Controller created.getId() aufruft für den Location-Header, müssen wir sicherstellen, dass ID nicht null ist.
        // Option A: Reflection nutzen (sauber).
        try {
            var field = com.example.demo.Gegenstand.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(created, 1L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        when(service.create(any(GegenstandCreateDto.class))).thenReturn(created);

        mockMvc.perform(post("/gegenstaende")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    // --- TEST 5: Validierung fehlgeschlagen (Kategorie fehlt) ---
    @Test
    void testCreate_ValidationFail() throws Exception {
        // Hier fehlt "kategorie", was @NotNull ist
        String invalidJson = """
            {
                "name": "Ding",
                "ort": "Hier",
                "wichtigkeit": "WICHTIG"
            }
        """;

        mockMvc.perform(post("/gegenstaende")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest()); // 400
    }

    // --- TEST 6: Löschen ---
    @Test
    void testDelete() throws Exception {
        mockMvc.perform(delete("/gegenstaende/99"))
                .andExpect(status().isNoContent());

        verify(service).delete(99L);
    }
}