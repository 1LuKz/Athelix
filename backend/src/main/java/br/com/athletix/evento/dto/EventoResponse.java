package br.com.athletix.evento.dto;

import br.com.athletix.evento.entity.Evento;
import br.com.athletix.evento.entity.StatusEvento;
import br.com.athletix.shared.api.dto.ResponseDto;
import br.com.athletix.shared.money.MoneyValues;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public record EventoResponse(Long id, Long atleticaId, Long gestaoOrigemId, String nome, String descricao,
                             OffsetDateTime inicio, OffsetDateTime fim, String local, String cidade, String uf,
                             String orcamentoPrevisto, Integer participantesEsperados, StatusEvento status,
                             Long criadoPorMembroId, Long version, Instant createdAt, Instant updatedAt) implements ResponseDto {
    public static EventoResponse from(Evento evento) {
        ZoneId fusoHorario = ZoneId.of(evento.getFusoHorarioAtletica());
        return new EventoResponse(evento.getId(), evento.getAtleticaId(), evento.getGestaoOrigemId(), evento.getNome(),
                evento.getDescricao(), evento.getInicio().atZoneSameInstant(fusoHorario).toOffsetDateTime(),
                evento.getFim().atZoneSameInstant(fusoHorario).toOffsetDateTime(), evento.getLocal(), evento.getCidade(),
                evento.getUf(), MoneyValues.format(evento.getOrcamentoPrevisto()), evento.getParticipantesEsperados(),
                evento.getStatus(), evento.getCriadoPorMembroId(), evento.getVersion(), evento.getCreatedAt(),
                evento.getUpdatedAt());
    }
}
