package br.com.athletix.evento.participacao.dto;

import br.com.athletix.shared.api.dto.ResponseDto;

/** Totais cumulativos: cada participação aparece uma vez em cada estágio que já alcançou. */
public record ParticipacaoEventoResumoResponse(Long eventoId, Integer participantesEsperados, long inscritos,
                                                long confirmados, long presentes) implements ResponseDto {
}
