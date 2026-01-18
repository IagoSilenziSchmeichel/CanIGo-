package com.example.demo;

import com.example.demo.dto.GegenstandCreateDto;
import com.example.demo.error.NotFoundException;
import com.example.demo.user.AppUser;
import com.example.demo.user.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GegenstandServiceIntegrationTest {

    @Autowired
    private GegenstandService service;

    @Autowired
    private AppUserRepository userRepo;

    @Autowired
    private GegenstandRepository gegenstandRepo;

    @Test
    void testDeleteForUser_Success() {
        // 1. User anlegen
        AppUser userEntity = new AppUser(null, "Tester", "test@delete.de", "abc");
        final AppUser savedUser = userRepo.save(userEntity);

        // 2. Gegenstand anlegen
        GegenstandCreateDto dto = new GegenstandCreateDto();
        dto.setName("Lösch-Mich");
        dto.setOrt("Mülleimer");
        dto.setWichtigkeit(Wichtigkeit.MUELL);
        dto.setKategorie(GegenstandKategorie.SONSTIGES);

        Gegenstand created = service.createForUser(savedUser.getId(), dto);
        final Long id = created.getId();

        // 3. Löschen aufrufen
        assertDoesNotThrow(() -> service.deleteForUser(savedUser.getId(), id));

        // 4. Prüfen ob wirklich weg
        assertTrue(gegenstandRepo.findById(id).isEmpty());
    }

    @Test
    void testDeleteForUser_ShouldFail_WhenWrongOwner() {
        // 1. Zwei verschiedene User anlegen
        AppUser userA = userRepo.save(new AppUser(null, "User A", "a@test.de", "pw"));
        AppUser userB = userRepo.save(new AppUser(null, "User B", "b@test.de", "pw"));
        final Long idB = userB.getId();

        // 2. User A legt einen Gegenstand an
        GegenstandCreateDto dto = new GegenstandCreateDto();
        dto.setName("Besitz von A");
        dto.setOrt("Safe");
        dto.setWichtigkeit(Wichtigkeit.WICHTIG);
        dto.setKategorie(GegenstandKategorie.HAUSHALT);
        Gegenstand itemA = service.createForUser(userA.getId(), dto);
        final Long itemAId = itemA.getId();

        // 3. VERSUCH: User B will den Gegenstand von User A löschen
        // Wir erwarten eine NotFoundException, weil der Service prüft, ob ID + OWNER zusammenpassen
        assertThrows(NotFoundException.class, () -> {
            service.deleteForUser(idB, itemAId);
        });

        // 4. CHECK: Der Gegenstand von User A darf NICHT gelöscht worden sein!
        assertTrue(gegenstandRepo.findById(itemAId).isPresent(), "Gegenstand wurde fälschlicherweise gelöscht!");
    }
}