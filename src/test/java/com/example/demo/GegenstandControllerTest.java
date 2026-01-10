package com.example.demo;

import com.example.demo.dto.GegenstandCreateDto;
import com.example.demo.security.JwtAuthFilter;
import com.example.demo.security.SecurityConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = GegenstandController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class)
        }
)
@AutoConfigureMockMvc(addFilters = false)
class GegenstandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GegenstandService service;

    @BeforeEach
    void setAuth() {
        // principal = userId (Long), so wie dein JwtAuthFilter es setzt
        var auth = new UsernamePasswordAuthenticationToken(1L, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void clearAuth() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetAlle_Empty() throws Exception {
        when(service.getAllForUser(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/gegenstaende"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
    }

    @Test
    void testGetAlle_WithData() throws Exception {
        var item = new Gegenstand(
                "Hammer",
                "Keller",
                Wichtigkeit.WICHTIG,
                GegenstandKategorie.HAUSHALT,
                null, null, null, null
        );

        when(service.getAllForUser(1L)).thenReturn(List.of(item));

        mockMvc.perform(get("/gegenstaende"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Hammer"))
                .andExpect(jsonPath("$[0].wichtigkeit").value("WICHTIG"));
    }

    @Test
    void testGetOne() throws Exception {
        var item = new Gegenstand(
                "Buch",
                "Regal",
                Wichtigkeit.UNWICHTIG,
                GegenstandKategorie.SONSTIGES,
                null, null, null, null
        );

        when(service.getByIdForUser(1L, 10L)).thenReturn(item);

        mockMvc.perform(get("/gegenstaende/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Buch"));
    }

    @Test
    void testCreate_Success() throws Exception {
        String jsonBody = """
            {
              "name": "Neu",
              "ort": "Hier",
              "wichtigkeit": "WICHTIG",
              "kategorie": "SONSTIGES"
            }
        """;

        var created = new Gegenstand(
                "Neu",
                "Hier",
                Wichtigkeit.WICHTIG,
                GegenstandKategorie.SONSTIGES,
                null, null, null, null
        );

        setIdViaReflection(created, 1L);

        when(service.createForUser(eq(1L), any(GegenstandCreateDto.class))).thenReturn(created);

        mockMvc.perform(post("/gegenstaende")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/gegenstaende/1"));
    }

    @Test
    void testCreate_ValidationFail() throws Exception {
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
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDelete() throws Exception {
        doNothing().when(service).deleteForUser(1L, 99L);

        mockMvc.perform(delete("/gegenstaende/99"))
                .andExpect(status().isNoContent());

        verify(service).deleteForUser(1L, 99L);
    }

    private static void setIdViaReflection(Gegenstand g, Long id) {
        try {
            Field f = Gegenstand.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(g, id);
        } catch (Exception e) {
            throw new RuntimeException("Could not set id via reflection", e);
        }
    }
}