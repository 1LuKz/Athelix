package br.com.athletix.tarefa.dto;

import br.com.athletix.tarefa.entity.PrioridadeTarefa;
import br.com.athletix.tarefa.entity.StatusTarefa;
import br.com.athletix.tarefa.entity.Tarefa;

import java.time.LocalDate;
import java.util.List;

public record ChecklistEventoResponse(Long eventoId, int total, int concluidas, int progresso,
                                      String mensagem, List<Item> itens) {

    public record Item(Long id, String titulo, StatusTarefa status, boolean concluida, boolean atrasada,
                       PrioridadeTarefa prioridade, LocalDate prazo, Long responsavelMembroId, Long version) {
    }

    public static ChecklistEventoResponse from(Long eventoId, List<Tarefa> tarefas, LocalDate hoje) {
        int total = tarefas.size();
        int concluidas = (int) tarefas.stream().filter(t -> t.getStatus() == StatusTarefa.CONCLUIDO).count();
        int progresso = total == 0 ? 0 : (int) Math.round(concluidas * 100.0 / total);
        String mensagem = total == 0 ? "Nenhuma tarefa cadastrada para este evento."
                : concluidas + " de " + total + " tarefas concluídas.";
        List<Item> itens = tarefas.stream().map(t -> new Item(t.getId(), t.getTitulo(), t.getStatus(),
                t.getStatus() == StatusTarefa.CONCLUIDO, t.atrasada(hoje), t.getPrioridade(), t.getPrazo(),
                t.getResponsavelMembroId(), t.getVersion())).toList();
        return new ChecklistEventoResponse(eventoId, total, concluidas, progresso, mensagem, itens);
    }
}