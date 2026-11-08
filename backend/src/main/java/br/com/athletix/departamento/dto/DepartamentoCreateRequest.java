package br.com.athletix.departamento.dto;

import br.com.athletix.shared.api.dto.CreateRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartamentoCreateRequest(
        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.") String nome,
        @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres.") String descricao
) implements CreateRequest {
}
