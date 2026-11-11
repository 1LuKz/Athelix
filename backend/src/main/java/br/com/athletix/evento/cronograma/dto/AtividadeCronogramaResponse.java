package br.com.athletix.evento.cronograma.dto;

import br.com.athletix.evento.cronograma.entity.AtividadeCronograma;
import br.com.athletix.evento.cronograma.entity.StatusAtividadeCronograma;
import br.com.athletix.shared.api.dto.ResponseDto;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public record AtividadeCronogramaResponse(Long id, Long atleticaId, Long eventoId, String titulo, String descricao,
                                          OffsetDateTime inicio, OffsetDateTime fim, StatusAtividadeCronograma status,
                                          Integer ordem, Long version, Instant createdAt, Instant updatedAt) implements ResponseDto {
    public static AtividadeCronogramaResponse from(AtividadeCronograma atividade) {
        ZoneId fusoHorario = ZoneId.of(atividade.getFusoHorarioAtletica());
        return new AtividadeCronogramaResponse(atividade.getId(), atividade.getAtleticaId(), atividade.getEventoId(),
                atividade.getTitulo(), atividade.getDescricao(), atividade.getInicio().atZoneSameInstant(fusoHorario).toOffsetDateTime(),
                atividade.getFim() == null ? null : atividade.getFim().atZoneSameInstant(fusoHorario).toOffsetDateTime(),
                atividade.getStatus(), atividade.getOrdem(), atividade.getVersion(), atividade.getCreatedAt(),
                atividade.getUpdatedAt());
    }
}
