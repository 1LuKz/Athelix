package br.com.athletix.usuario.dto;

import br.com.athletix.shared.api.dto.CreateRequest;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Cria uma identidade ainda sem senha, destinada ao fluxo de convite. */
public record UsuarioConviteCreateRequest(
        @NotBlank(message = "O nome é obrigatório.")
        @Size(min = 3, max = 120, message = "O nome deve ter entre 3 e 120 caracteres.") String nome,
        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "O e-mail é inválido.")
        @Size(max = 254, message = "O e-mail deve ter no máximo 254 caracteres.") String email,
        @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres.") String telefone
) implements CreateRequest {
}
