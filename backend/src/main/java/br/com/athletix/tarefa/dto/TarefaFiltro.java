package br.com.athletix.tarefa.dto;

import br.com.athletix.tarefa.entity.PrioridadeTarefa;
import br.com.athletix.tarefa.entity.StatusTarefa;
import java.time.LocalDate;

public record TarefaFiltro(
    Long eventoId,
    Long responsavelMembroId,
    Long departamentoId,
    PrioridadeTarefa prioridade,
    StatusTarefa status,
    LocalDate prazoDe,
    LocalDate prazoAte,
    Boolean atrasada) {}
