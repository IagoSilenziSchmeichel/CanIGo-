package com.example.demo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface GegenstandRepository extends CrudRepository<Gegenstand, Long> {

    List<Gegenstand> findAll();

    // Reminder-Query (unabhängig vom Owner – später evtl. filtern)
    List<Gegenstand> findAllByWegwerfAmLessThanEqualAndErinnerungAktivTrueAndErinnerungGesendetFalse(LocalDate date);

    // ✅ Multi-User: alles nur für Owner
    List<Gegenstand> findAllByOwner_Id(Long ownerId);

    Optional<Gegenstand> findByIdAndOwner_Id(Long id, Long ownerId);

    boolean existsByIdAndOwner_Id(Long id, Long ownerId);

    void deleteByIdAndOwner_Id(Long id, Long ownerId);
}