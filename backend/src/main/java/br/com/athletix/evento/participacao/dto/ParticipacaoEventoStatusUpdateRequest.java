package br.com.athletix.evento.participacao.dto;

import br.com.athletix.evento.participacao.entity.StatusParticipacaoEvento;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ParticipacaoEventoStatusUpdateRequest(
    @NotNull StatusParticipacaoEvento status,
    @Size(max = 500) String motivoCorrecao,
    @NotNull @PositiveOrZero Long version) {}
