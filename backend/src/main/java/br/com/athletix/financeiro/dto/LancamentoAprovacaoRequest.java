package br.com.athletix.financeiro.dto;

import br.com.athletix.financeiro.entity.DecisaoAprovacaoFinanceira;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record LancamentoAprovacaoRequest(
        @NotNull(message = "A decisão é obrigatória.") DecisaoAprovacaoFinanceira decisao,
        @NotBlank(message = "O motivo da decisão é obrigatório.")
        @Size(max = 1000, message = "O motivo deve ter no máximo 1000 caracteres.") String motivo,
        @NotNull(message = "A versão é obrigatória.")
        @PositiveOrZero(message = "A versão deve ser maior ou igual a zero.") Long version
) {
}
