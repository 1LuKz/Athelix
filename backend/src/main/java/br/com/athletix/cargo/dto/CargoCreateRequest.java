package br.com.athletix.cargo.dto;

import br.com.athletix.cargo.entity.PerfilAcesso;
import br.com.athletix.shared.api.dto.CreateRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CargoCreateRequest(
    @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
        String nome,
    @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres.") String descricao,
    @NotNull(message = "O perfil de acesso é obrigatório.") PerfilAcesso perfilAcesso,
    @NotNull(message = "A situação do cargo é obrigatória.") Boolean ativo)
    implements CreateRequest {}
