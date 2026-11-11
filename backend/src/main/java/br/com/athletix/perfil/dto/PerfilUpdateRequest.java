package br.com.athletix.perfil.dto;

import br.com.athletix.shared.api.dto.UpdateRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record PerfilUpdateRequest(
        @NotBlank @Size(min = 3, max = 120) String nome,
        @Size(max = 20) String telefone,
        @NotBlank @Size(max = 160) String curso,
        @Positive @Max(30) Integer periodoCurso,
        @NotNull @PositiveOrZero Long usuarioVersion,
        @NotNull @PositiveOrZero Long membroVersion
) implements UpdateRequest {
    @Override
    public Long version() {
        return membroVersion;
    }
}
