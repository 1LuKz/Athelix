package br.com.athletix.financeiro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record EstornoFinanceiroCreateRequest(
    @NotNull(message = "A data do estorno é obrigatória.") LocalDate dataEstorno,
    @NotBlank(message = "O motivo do estorno é obrigatório.")
        @Size(max = 1000, message = "O motivo deve ter no máximo 1000 caracteres.")
        String motivo) {}
