package br.com.athletix.patrimonio.conferencia.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ConferenciaEncerramentoRequest(
        @NotNull @PositiveOrZero Long version) {
}
