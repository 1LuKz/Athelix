package br.com.athletix.evento.responsabilidade.dto;

import br.com.athletix.evento.responsabilidade.entity.AreaResponsabilidadeEvento;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ResponsabilidadeEventoCreateRequest(
        @NotNull @Positive Long membroId,
        @NotNull AreaResponsabilidadeEvento area
) {
}
