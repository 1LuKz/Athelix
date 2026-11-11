package br.com.athletix.evento.dto;

import br.com.athletix.evento.entity.StatusEvento;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record EventoStatusUpdateRequest(
        @NotNull StatusEvento status,
        @Size(max = 500) String motivoCorrecao,
        @NotNull @PositiveOrZero Long version
) {
}
