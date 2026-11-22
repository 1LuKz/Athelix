package br.com.athletix.config.demo;

import static br.com.athletix.config.demo.DemoData.*;

import br.com.athletix.config.demo.EventosDemoSeeder.Eventos;
import br.com.athletix.evento.entity.Evento;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.tarefa.dto.TarefaRequests;
import br.com.athletix.tarefa.dto.TarefaResponse;
import br.com.athletix.tarefa.entity.PrioridadeTarefa;
import br.com.athletix.tarefa.entity.StatusTarefa;
import br.com.athletix.tarefa.service.TarefaService;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
class TarefasDemoSeeder {
  private final TarefaService tarefas;

  TarefasDemoSeeder(TarefaService tarefas) {
    this.tarefas = tarefas;
  }

  void criar(CenarioDemo cenario, Eventos eventos) {
    criarChecklist(cenario, eventos.andamento());
    for (TarefaDemo dados : TAREFAS_COMPLEMENTARES) {
      Evento evento =
          switch (dados.evento()) {
            case PLANEJADO -> eventos.planejado();
            case INSCRICOES_ABERTAS -> eventos.inscricoes();
            case EM_ANDAMENTO -> eventos.andamento();
            case CONCLUIDO -> eventos.concluido();
          };
      Membro responsavel =
          switch (dados.responsavel()) {
            case ADMINISTRADOR -> cenario.administrador();
            case TESOUREIRO -> cenario.tesoureiro();
            case GESTORA -> cenario.gestora();
            case ASSOCIADO -> cenario.associado();
          };
      criar(
          cenario,
          evento,
          responsavel,
          dados.titulo(),
          dados.status(),
          dados.prioridade(),
          dados.deslocamentoPrazo());
    }
  }

  private void criarChecklist(CenarioDemo cenario, Evento evento) {
    List<Membro> responsaveis =
        List.of(
            cenario.administrador(), cenario.gestora(), cenario.associado(), cenario.tesoureiro());
    PrioridadeTarefa[] prioridades = PrioridadeTarefa.values();
    for (int indice = 0; indice < CHECKLIST_CONCLUIDO.size(); indice++) {
      criar(
          cenario,
          evento,
          responsaveis.get(indice % responsaveis.size()),
          CHECKLIST_CONCLUIDO.get(indice),
          StatusTarefa.CONCLUIDO,
          prioridades[indice % prioridades.length],
          PRAZO_CHECKLIST_CONCLUIDO_DIAS);
    }
  }

  private void criar(
      CenarioDemo cenario,
      Evento evento,
      Membro responsavel,
      String titulo,
      StatusTarefa status,
      PrioridadeTarefa prioridade,
      int deslocamentoPrazo) {
    TarefaResponse tarefa =
        tarefas.criar(
            new TarefaRequests.Create(
                evento.getId(),
                titulo,
                TAREFA_SINTETICA_PARA_DEMONSTRACAO,
                responsavel.getId(),
                cenario.esportes().getId(),
                prioridade,
                cenario.hoje().plusDays(deslocamentoPrazo)),
            cenario.administrador());
    aplicarStatus(cenario, tarefa, status);
  }

  private void aplicarStatus(CenarioDemo cenario, TarefaResponse tarefa, StatusTarefa status) {
    if (status == StatusTarefa.A_FAZER) {
      return;
    }
    if (status == StatusTarefa.REVISAO) {
      tarefa =
          tarefas.alterarStatus(
              tarefa.id(),
              new TarefaRequests.Status(StatusTarefa.EM_ANDAMENTO, null, tarefa.version()),
              cenario.administrador());
    }
    tarefas.alterarStatus(
        tarefa.id(),
        new TarefaRequests.Status(status, null, tarefa.version()),
        cenario.administrador());
  }
}
