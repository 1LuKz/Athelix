package br.com.athletix.tarefa.dto;

import br.com.athletix.tarefa.entity.PrioridadeTarefa;
import br.com.athletix.tarefa.entity.StatusTarefa;
import br.com.athletix.tarefa.entity.Tarefa;
import java.time.Instant;
import java.time.LocalDate;

public record TarefaResponse(
    Long id,
    Long eventoId,
    Long gestaoOrigemId,
    Long responsavelMembroId,
    Long departamentoId,
    String titulo,
    String descricao,
    PrioridadeTarefa prioridade,
    LocalDate prazo,
    StatusTarefa status,
    boolean atrasada,
    Instant concluidaEm,
    Long criadaPorMembroId,
    Instant criadaEm,
    Instant atualizadaEm,
    Long version) {

  public static TarefaResponse from(Tarefa t, LocalDate hoje) {
    return new TarefaResponse(
        t.getId(),
        t.getEventoId(),
        t.getGestaoOrigemId(),
        t.getResponsavelMembroId(),
        t.getDepartamentoId(),
        t.getTitulo(),
        t.getDescricao(),
        t.getPrioridade(),
        t.getPrazo(),
        t.getStatus(),
        t.atrasada(hoje),
        t.getConcluidaEm(),
        t.getCriadaPorMembroId(),
        t.getCreatedAt(),
        t.getUpdatedAt(),
        t.getVersion());
  }
}
