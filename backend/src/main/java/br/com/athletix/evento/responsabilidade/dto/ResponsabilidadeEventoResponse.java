package br.com.athletix.evento.responsabilidade.dto;

import br.com.athletix.evento.responsabilidade.entity.AreaResponsabilidadeEvento;
import br.com.athletix.evento.responsabilidade.entity.ResponsabilidadeEvento;
import br.com.athletix.shared.api.dto.ResponseDto;

import java.time.Instant;

public record ResponsabilidadeEventoResponse(Long id, Long atleticaId, Long eventoId, Long membroId,
                                             String membroNome, AreaResponsabilidadeEvento area, boolean ativa,
                                             Instant encerradaEm, Long encerradaPorMembroId, Long version,
                                             Instant createdAt, Instant updatedAt) implements ResponseDto {
    public static ResponsabilidadeEventoResponse from(ResponsabilidadeEvento responsabilidade) {
        return new ResponsabilidadeEventoResponse(responsabilidade.getId(), responsabilidade.getAtleticaId(),
                responsabilidade.getEventoId(), responsabilidade.getMembroId(), responsabilidade.getNomeMembro(),
                responsabilidade.getArea(), responsabilidade.isAtiva(), responsabilidade.getEncerradaEm(),
                responsabilidade.getEncerradaPorMembroId(), responsabilidade.getVersion(),
                responsabilidade.getCreatedAt(), responsabilidade.getUpdatedAt());
    }
}
