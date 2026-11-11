package br.com.athletix.perfil.dto;

import br.com.athletix.shared.api.dto.UpdateRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record PerfilSenhaUpdateRequest(
        @NotBlank String senhaAtual,
        @NotBlank String novaSenha,
        @NotNull @PositiveOrZero Long version
) implements UpdateRequest { }
