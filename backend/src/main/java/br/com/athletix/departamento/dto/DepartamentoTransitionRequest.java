package br.com.athletix.departamento.dto;

import br.com.athletix.shared.api.dto.UpdateRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record DepartamentoTransitionRequest(
    @NotNull(message = "A versão é obrigatória.")
        @PositiveOrZero(message = "A versão deve ser maior ou igual a zero.")
        Long version)
    implements UpdateRequest {}
