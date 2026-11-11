package br.com.athletix.tarefa.dto;
import br.com.athletix.tarefa.entity.*; import java.time.*;
public record TarefaResponse(Long id,Long eventoId,Long responsavelMembroId,Long departamentoId,String titulo,String descricao,PrioridadeTarefa prioridade,LocalDate prazo,StatusTarefa status,Instant concluidaEm,Long version){public static TarefaResponse from(Tarefa t){return new TarefaResponse(t.getId(),t.getEventoId(),t.getResponsavelMembroId(),t.getDepartamentoId(),t.getTitulo(),t.getDescricao(),t.getPrioridade(),t.getPrazo(),t.getStatus(),t.getConcluidaEm(),t.getVersion());}}
