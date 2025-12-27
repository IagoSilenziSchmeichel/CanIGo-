package com.example.demo;

import com.example.demo.dto.GegenstandCreateDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GegenstandServiceTest {

    @Mock
    private GegenstandRepository repo;

    @InjectMocks
    private GegenstandService service;

    // --- TEST 7: Service speichert korrekt mit DTO ---
    @Test
    void shouldSaveGegenstand() {
        // GIVEN
        GegenstandCreateDto dto = new GegenstandCreateDto();
        dto.setName("ServiceTest");
        dto.setOrt("Labor");
        dto.setWichtigkeit(Wichtigkeit.MUELL);
        dto.setKategorie(GegenstandKategorie.SONSTIGES);

        // Mock: Wenn repo.save aufgerufen wird, gib das Objekt zurück, das reinkam
        when(repo.save(any(Gegenstand.class))).thenAnswer(i -> i.getArguments()[0]);

        // WHEN
        Gegenstand result = service.create(dto);

        // THEN
        verify(repo).save(any(Gegenstand.class));
        assertEquals("ServiceTest", result.getName());
        assertEquals(Wichtigkeit.MUELL, result.getWichtigkeit());
        assertEquals(GegenstandKategorie.SONSTIGES, result.getKategorie());
    }
}