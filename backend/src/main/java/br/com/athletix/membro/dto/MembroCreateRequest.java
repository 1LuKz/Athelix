package br.com.athletix.membro.dto;

import br.com.athletix.shared.api.dto.CreateRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record MembroCreateRequest(
    @NotNull(message = "O usuário é obrigatório.") @Positive(message = "O usuário deve ser válido.")
        Long usuarioId,
    @NotBlank(message = "O curso é obrigatório.")
        @Size(max = 160, message = "O curso deve ter no máximo 160 caracteres.")
        String curso,
    @Positive(message = "O período deve ser maior que zero.")
        @Max(value = 30, message = "O período deve ser no máximo 30.")
        Integer periodoCurso,
    @NotNull(message = "O cargo é obrigatório.") @Positive(message = "O cargo deve ser válido.")
        Long cargoId,
    @NotNull(message = "O departamento é obrigatório.")
        @Positive(message = "O departamento deve ser válido.")
        Long departamentoId,
    LocalDate membroDesde)
    implements CreateRequest {}
