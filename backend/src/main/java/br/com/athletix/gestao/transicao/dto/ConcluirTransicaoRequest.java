package br.com.athletix.gestao.transicao.dto;

import jakarta.validation.constraints.*;

public record ConcluirTransicaoRequest(@NotNull @PositiveOrZero Long version) {}
