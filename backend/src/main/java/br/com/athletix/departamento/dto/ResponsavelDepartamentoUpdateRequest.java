package br.com.athletix.departamento.dto;

import br.com.athletix.shared.api.dto.UpdateRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

/** O ID será convertido em relação com Membro na B05. */
public record ResponsavelDepartamentoUpdateRequest(
    @NotNull(message = "O responsável é obrigatório.")
        @Positive(message = "O responsável deve ser válido.")
        Long responsavelMembroId,
    @NotNull(message = "A versão é obrigatória.")
        @PositiveOrZero(message = "A versão deve ser maior ou igual a zero.")
        Long version)
    implements UpdateRequest {}
