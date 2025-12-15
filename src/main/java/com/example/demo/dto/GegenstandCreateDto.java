package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;

public class GegenstandCreateDto {
    @NotBlank
    private String name;

    @NotBlank
    private String ort;

    private String status;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOrt() { return ort; }
    public void setOrt(String ort) { this.ort = ort; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}