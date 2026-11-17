package br.com.athletix.atletica.dto;

import br.com.athletix.shared.api.dto.CreateRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DominioEmailPermitidoCreateRequest(
    @NotBlank(message = "O domínio de e-mail é obrigatório.")
        @Size(max = 253, message = "O domínio de e-mail deve ter no máximo 253 caracteres.")
        String dominio)
    implements CreateRequest {}
