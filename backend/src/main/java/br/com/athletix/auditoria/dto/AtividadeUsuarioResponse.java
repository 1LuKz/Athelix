package br.com.athletix.auditoria.dto;

import br.com.athletix.auditoria.entity.AtividadeUsuario;
import br.com.athletix.shared.api.dto.ResponseDto;

import java.time.Instant;

public record AtividadeUsuarioResponse(Long id, String acao, String tipoRecurso, Long recursoId,
                                       String resumo, Instant ocorridaEm, Long gestaoId) implements ResponseDto {
    public static AtividadeUsuarioResponse from(AtividadeUsuario atividade) {
        return new AtividadeUsuarioResponse(atividade.getId(), atividade.getAcao(), atividade.getTipoRecurso(),
                atividade.getRecursoId(), atividade.getResumo(), atividade.getOcorridaEm(), atividade.getGestaoId());
    }
}
