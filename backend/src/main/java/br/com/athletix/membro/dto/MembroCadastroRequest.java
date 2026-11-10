package br.com.athletix.membro.dto;

import br.com.athletix.shared.api.dto.CreateRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Cadastro administrativo. Dados pessoais só criam uma nova identidade se o e-mail ainda não existir. */
public record MembroCadastroRequest(
        @NotBlank(message = "O nome é obrigatório.")
        @Size(min = 3, max = 120, message = "O nome deve ter entre 3 e 120 caracteres.") String nome,
        @NotBlank(message = "O e-mail é obrigatório.")
        @Size(max = 254, message = "O e-mail deve ter no máximo 254 caracteres.") String email,
        @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres.") String telefone,
        @NotBlank(message = "O curso é obrigatório.")
        @Size(max = 160, message = "O curso deve ter no máximo 160 caracteres.") String curso,
        @Positive(message = "O período deve ser maior que zero.")
        @Max(value = 30, message = "O período deve ser no máximo 30.") Integer periodoCurso,
        @NotNull(message = "O cargo é obrigatório.") @Positive(message = "O cargo deve ser válido.") Long cargoId,
        @NotNull(message = "O departamento é obrigatório.") @Positive(message = "O departamento deve ser válido.") Long departamentoId,
        LocalDate membroDesde
) implements CreateRequest {
}
