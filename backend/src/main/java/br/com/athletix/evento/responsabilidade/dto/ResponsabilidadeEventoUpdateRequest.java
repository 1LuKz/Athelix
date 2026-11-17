package br.com.athletix.evento.responsabilidade.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record ResponsabilidadeEventoUpdateRequest(
    @NotNull @Positive Long membroId, @NotNull @PositiveOrZero Long version) {}
