package br.com.athletix.evento.participacao.dto;

import br.com.athletix.evento.participacao.entity.ParticipacaoEvento;
import br.com.athletix.evento.participacao.entity.StatusParticipacaoEvento;
import br.com.athletix.shared.api.dto.ResponseDto;

import java.time.Instant;

public record ParticipacaoEventoResponse(Long id, Long atleticaId, Long eventoId, Long membroId, String nomeParticipante,
                                         String emailParticipante, StatusParticipacaoEvento status, Instant inscritoEm,
                                         Instant confirmadoEm, Instant presencaEm, Long version, Instant createdAt,
                                         Instant updatedAt) implements ResponseDto {
    public static ParticipacaoEventoResponse from(ParticipacaoEvento participacao) {
        return new ParticipacaoEventoResponse(participacao.getId(), participacao.getAtleticaId(), participacao.getEventoId(),
                participacao.getMembroId(), participacao.getNomeParticipante(), participacao.getEmailParticipante(), participacao.getStatus(),
                participacao.getInscritoEm(), participacao.getConfirmadoEm(), participacao.getPresencaEm(),
                participacao.getVersion(), participacao.getCreatedAt(), participacao.getUpdatedAt());
    }
}
