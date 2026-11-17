package br.com.athletix.patrimonio.manutencao.dto;

import jakarta.validation.constraints.*;

public record ManutencaoConclusaoRequest(
    @NotBlank @Size(max = 1000) String resultado, @NotNull @PositiveOrZero Long version) {}
