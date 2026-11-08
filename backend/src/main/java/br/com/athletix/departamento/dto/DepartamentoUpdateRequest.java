package br.com.athletix.departamento.dto;

import br.com.athletix.shared.api.dto.UpdateRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record DepartamentoUpdateRequest(
        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.") String nome,
        @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres.") String descricao,
        @NotNull(message = "A versão é obrigatória.")
        @PositiveOrZero(message = "A versão deve ser maior ou igual a zero.") Long version
) implements UpdateRequest {
}
