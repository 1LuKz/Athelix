package br.com.athletix.patrimonio.conferencia.dto;

import jakarta.validation.constraints.*;

public record ConferenciaCreateRequest(@NotBlank @Size(max = 120) String nome) {}
