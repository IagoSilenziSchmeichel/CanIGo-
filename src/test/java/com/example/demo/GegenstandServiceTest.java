package com.example.demo;

import com.example.demo.dto.GegenstandCreateDto;
import com.example.demo.user.AppUser;
import com.example.demo.user.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GegenstandServiceTest {

    @Mock
    private GegenstandRepository repo;

    @Mock
    private AppUserRepository userRepo;

    @InjectMocks
    private GegenstandService service;

    @Test
    void shouldSaveGegenstandForUser() {
        // GIVEN
        Long userId = 1L;

        GegenstandCreateDto dto = new GegenstandCreateDto();
        dto.setName("ServiceTest");
        dto.setOrt("Labor");
        dto.setWichtigkeit(Wichtigkeit.MUELL);
        dto.setKategorie(GegenstandKategorie.SONSTIGES);

        AppUser owner = new AppUser();
        owner.setId(userId);
        owner.setName("Test User");
        owner.setEmail("test@example.com");
        owner.setPasswordHash("hash");

        when(userRepo.findById(userId)).thenReturn(Optional.of(owner));
        when(repo.save(any(Gegenstand.class))).thenAnswer(i -> i.getArguments()[0]);

        // WHEN
        Gegenstand result = service.createForUser(userId, dto);

        // THEN
        verify(userRepo).findById(userId);
        verify(repo).save(any(Gegenstand.class));

        assertNotNull(result);
        assertEquals("ServiceTest", result.getName());
        assertEquals("Labor", result.getOrt());
        assertEquals(Wichtigkeit.MUELL, result.getWichtigkeit());
        assertEquals(GegenstandKategorie.SONSTIGES, result.getKategorie());

        assertNotNull(result.getOwner());
        assertEquals(userId, result.getOwner().getId());
    }
}