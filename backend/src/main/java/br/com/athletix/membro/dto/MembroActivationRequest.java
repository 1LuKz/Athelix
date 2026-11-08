package br.com.athletix.membro.dto;

import br.com.athletix.shared.api.dto.UpdateRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

/** O validador pode ser nulo no fluxo excepcional do primeiro membro em configuração. */
public record MembroActivationRequest(
        @Positive(message = "O validador deve ser válido.") Long validadoPorMembroId,
        @NotNull(message = "A versão é obrigatória.")
        @PositiveOrZero(message = "A versão deve ser maior ou igual a zero.") Long version
) implements UpdateRequest {
}
