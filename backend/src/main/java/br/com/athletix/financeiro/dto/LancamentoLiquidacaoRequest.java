package br.com.athletix.financeiro.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;

public record LancamentoLiquidacaoRequest(
    @NotNull(message = "A data de liquidação é obrigatória.") LocalDate dataLiquidacao,
    @NotNull(message = "A versão é obrigatória.")
        @PositiveOrZero(message = "A versão deve ser maior ou igual a zero.")
        Long version) {}
