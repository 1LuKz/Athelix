package br.com.athletix.patrimonio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoriaPatrimonioCreateRequest(
    @NotBlank @Size(max = 100) String nome, @NotNull Boolean ativa) {}
