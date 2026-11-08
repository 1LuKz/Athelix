package br.com.athletix.shared.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Contrato para alterações concorrentes. Todo DTO de alteração deve transportar a versão lida.
 */
public interface UpdateRequest {

    @NotNull(message = "A versão é obrigatória.")
    @PositiveOrZero(message = "A versão deve ser maior ou igual a zero.")
    Long version();
}
