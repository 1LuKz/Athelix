package br.com.athletix.financeiro.dto;

import br.com.athletix.shared.api.dto.UpdateRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record LancamentoFinanceiroUpdateRequest(
        @Positive(message = "O evento deve ser válido.") Long eventoId,
        @NotBlank(message = "A descrição é obrigatória.")
        @Size(max = 250, message = "A descrição deve ter no máximo 250 caracteres.") String descricao,
        @NotNull(message = "A categoria é obrigatória.")
        @Positive(message = "A categoria deve ser válida.") Long categoriaId,
        @NotBlank(message = "O valor é obrigatório.") String valor,
        @NotNull(message = "A data de competência é obrigatória.") LocalDate dataCompetencia,
        LocalDate dataVencimento,
        @NotNull(message = "A versão é obrigatória.")
        @PositiveOrZero(message = "A versão deve ser maior ou igual a zero.") Long version
) implements UpdateRequest {
}
