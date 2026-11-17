package br.com.athletix.auth.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SelecionarAtleticaRequest(
    @NotNull(message = "O vínculo da atlética é obrigatório.")
        @Positive(message = "O vínculo da atlética deve ser positivo.")
        Long membroId) {}
