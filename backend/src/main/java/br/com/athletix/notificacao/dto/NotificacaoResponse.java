package br.com.athletix.notificacao.dto;

import java.time.Instant;
import java.time.LocalDate;

import br.com.athletix.notificacao.entity.Notificacao;
import br.com.athletix.notificacao.entity.TipoNotificacao;

public record NotificacaoResponse(Long id, Long tarefaId, TipoNotificacao tipo, LocalDate dataReferencia,
                                  String titulo, String mensagem, Instant criadaEm, Instant lidaEm) {

    public record Resumo(long naoLidas) {
    }

    public static NotificacaoResponse from(Notificacao n) {
        return new NotificacaoResponse(n.getId(), n.getTarefaId(), n.getTipo(), n.getDataReferencia(),
                n.getTitulo(), n.getMensagem(), n.getCreatedAt(), n.getLidaEm());
    }
}