package com.example.demo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GegenstandRepository extends CrudRepository<Gegenstand, Long> {
    // Hier zaubert Spring Boot alle Methoden (save, findAll, etc.) automatisch!
    List<Gegenstand> findAll();
}