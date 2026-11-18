package br.com.athletix.config.demo;

import static br.com.athletix.config.demo.DemoData.*;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.config.demo.EventosDemoSeeder.Eventos;
import br.com.athletix.departamento.entity.Departamento;
import br.com.athletix.evento.entity.Evento;
import br.com.athletix.gestao.entity.Gestao;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.notificacao.entity.Notificacao;
import br.com.athletix.notificacao.entity.TipoNotificacao;
import br.com.athletix.notificacao.repository.NotificacaoRepository;
import br.com.athletix.tarefa.entity.PrioridadeTarefa;
import br.com.athletix.tarefa.entity.StatusTarefa;
import br.com.athletix.tarefa.entity.Tarefa;
import br.com.athletix.tarefa.repository.TarefaRepository;
import java.time.LocalDate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
class TarefasDemoSeeder {
  private final TarefaRepository tarefaRepository;
  private final NotificacaoRepository notificacaoRepository;

  TarefasDemoSeeder(
      TarefaRepository tarefaRepository, NotificacaoRepository notificacaoRepository) {
    this.tarefaRepository = tarefaRepository;
    this.notificacaoRepository = notificacaoRepository;
  }

  void criar(
      Atletica engenharia,
      Gestao atual,
      Eventos eventos,
      Membro gestorMembro,
      Membro membroAtivo,
      Departamento esportes,
      LocalDate hoje) {
    tarefaRepository.save(
        tarefa(
            engenharia,
            atual,
            eventos.planejado(),
            gestorMembro,
            esportes,
            RESERVAR_QUADRA,
            PrioridadeTarefa.ALTA,
            hoje.plusDays(PRAZO_RESERVA_DIAS)));
    Tarefa divulgacao =
        tarefaRepository.save(
            tarefa(
                engenharia,
                atual,
                eventos.inscricoes(),
                membroAtivo,
                esportes,
                DIVULGAR_INSCRICOES,
                PrioridadeTarefa.MEDIA,
                hoje.plusDays(PRAZO_DIVULGACAO_DIAS)));
    divulgacao.status(StatusTarefa.EM_ANDAMENTO);
    tarefaRepository.save(divulgacao);
    Tarefa conferenciaMateriais =
        tarefaRepository.save(
            tarefa(
                engenharia,
                atual,
                eventos.andamento(),
                gestorMembro,
                esportes,
                CONFERIR_MATERIAIS,
                PrioridadeTarefa.MEDIA,
                hoje.minusDays(ATRASO_CONFERENCIA_MATERIAIS_DIAS)));
    conferenciaMateriais.status(StatusTarefa.REVISAO);
    tarefaRepository.save(conferenciaMateriais);
    Tarefa publicacao =
        tarefaRepository.save(
            tarefa(
                engenharia,
                atual,
                eventos.concluido(),
                membroAtivo,
                esportes,
                PUBLICAR_RESULTADOS,
                PrioridadeTarefa.BAIXA,
                hoje.minusDays(PRAZO_RESULTADOS_DIAS)));
    publicacao.status(StatusTarefa.CONCLUIDO);
    tarefaRepository.save(publicacao);
    notificacaoRepository.save(
        new Notificacao(
            engenharia.getId(),
            gestorMembro.getId(),
            conferenciaMateriais.getId(),
            TipoNotificacao.ATRASADA,
            hoje,
            TAREFA_ATRASADA,
            CONFERIR_MATERIAIS_ESTA_COM_PRAZO_VENCIDO));
  }

  private Tarefa tarefa(
      Atletica atletica,
      Gestao gestao,
      Evento evento,
      Membro responsavel,
      Departamento departamento,
      String titulo,
      PrioridadeTarefa prioridade,
      LocalDate prazo) {
    return new Tarefa(
        atletica,
        gestao.getId(),
        evento,
        titulo,
        TAREFA_SINTETICA_PARA_DEMONSTRACAO,
        responsavel,
        departamento,
        prioridade,
        prazo,
        responsavel.getId());
  }
}
