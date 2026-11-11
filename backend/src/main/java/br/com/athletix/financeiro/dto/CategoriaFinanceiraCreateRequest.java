package br.com.athletix.financeiro.dto;

import br.com.athletix.financeiro.entity.TipoAplicavelCategoria;
import br.com.athletix.shared.api.dto.CreateRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoriaFinanceiraCreateRequest(
        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.") String nome,
        @NotNull(message = "O tipo aplicável é obrigatório.") TipoAplicavelCategoria tipoAplicavel,
        @NotNull(message = "A situação da categoria é obrigatória.") Boolean ativa
) implements CreateRequest {
}
