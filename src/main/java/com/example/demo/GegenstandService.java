package com.example.demo;

import com.example.demo.dto.GegenstandCreateDto;
import com.example.demo.error.NotFoundException;
import com.example.demo.user.AppUser;
import com.example.demo.user.AppUserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class GegenstandService {

    private final GegenstandRepository repo;
    private final AppUserRepository userRepo;

    public GegenstandService(GegenstandRepository repo, AppUserRepository userRepo) {
        this.repo = repo;
        this.userRepo = userRepo;
    }

    // ✅ Multi-User: nur eigene Gegenstände
    public List<Gegenstand> getAllForUser(Long userId) {
        return repo.findAllByOwner_Id(userId);
    }

    public Gegenstand getByIdForUser(Long userId, Long id) {
        return repo.findByIdAndOwner_Id(id, userId)
                .orElseThrow(() -> new NotFoundException("Gegenstand nicht gefunden: " + id));
    }

    public Gegenstand createForUser(Long userId, GegenstandCreateDto dto) {
        AppUser owner = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User nicht gefunden: " + userId));

        Gegenstand g = new Gegenstand();
        g.setOwner(owner); // ✅ Ownership setzen
        applyDto(g, dto);

        return repo.save(g);
    }

    public Gegenstand updateForUser(Long userId, Long id, GegenstandCreateDto dto) {
        Gegenstand g = getByIdForUser(userId, id); // ✅ Ownership check
        applyDto(g, dto);
        return repo.save(g);
    }

    public void deleteForUser(Long userId, Long id) {
        if (!repo.existsByIdAndOwner_Id(id, userId)) {
            throw new NotFoundException("Gegenstand nicht gefunden: " + id);
        }
        repo.deleteByIdAndOwner_Id(id, userId);
    }

    public boolean istWegwerfbar(LocalDate lastUsed) {
        return lastUsed != null && lastUsed.isBefore(LocalDate.now().minusMonths(6));
    }

    private void applyDto(Gegenstand g, GegenstandCreateDto dto) {
        g.setName(dto.getName());
        g.setOrt(dto.getOrt());
        g.setWichtigkeit(dto.getWichtigkeit());
        g.setKategorie(dto.getKategorie());
        g.setLastUsed(dto.getLastUsed());
        g.setWegwerfAm(dto.getWegwerfAm());
        g.setKaufpreis(dto.getKaufpreis());
        g.setWunschVerkaufpreis(dto.getWunschVerkaufpreis());
    }
}