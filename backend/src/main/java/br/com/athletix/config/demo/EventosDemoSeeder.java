package br.com.athletix.config.demo;

import static br.com.athletix.config.demo.DemoData.*;

import br.com.athletix.cargo.entity.PerfilAcesso;
import br.com.athletix.evento.dto.EventoCreateRequest;
import br.com.athletix.evento.dto.EventoStatusUpdateRequest;
import br.com.athletix.evento.entity.Evento;
import br.com.athletix.evento.entity.StatusEvento;
import br.com.athletix.evento.service.EventoService;
import jakarta.persistence.EntityManager;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
class EventosDemoSeeder {
  private final EventoService eventos;
  private final EntityManager persistencia;

  EventosDemoSeeder(EventoService eventos, EntityManager persistencia) {
    this.eventos = eventos;
    this.persistencia = persistencia;
  }

  Eventos criar(CenarioDemo cenario) {
    OffsetDateTime agora =
        cenario.agora().atZone(ZoneId.of(cenario.atletica().getFusoHorario())).toOffsetDateTime();
    Evento planejado =
        criar(
            cenario,
            TORNEIO_DE_INTEGRACAO,
            agora.plusDays(INICIO_TORNEIO_DIAS),
            agora.plusDays(FIM_TORNEIO_DIAS));
    Evento inscricoes =
        criar(
            cenario, COPA_ABERTA, agora.plusDays(INICIO_COPA_DIAS), agora.plusDays(FIM_COPA_DIAS));
    alterarStatus(inscricoes, StatusEvento.INSCRICOES_ABERTAS);
    Evento andamento =
        criar(
            cenario,
            SEMANA_ESPORTIVA,
            agora.minusHours(INICIO_SEMANA_ESPORTIVA_HORAS),
            agora.plusDays(FIM_SEMANA_ESPORTIVA_DIAS));
    alterarStatus(andamento, StatusEvento.EM_ANDAMENTO);
    Evento concluido =
        criar(
            cenario,
            JOGOS_DO_SEMESTRE,
            agora.minusDays(INICIO_JOGOS_DIAS),
            agora.minusDays(FIM_JOGOS_DIAS));
    alterarStatus(concluido, StatusEvento.EM_ANDAMENTO);
    alterarStatus(concluido, StatusEvento.CONCLUIDO);
    return new Eventos(planejado, inscricoes, andamento, concluido);
  }

  private Evento criar(
      CenarioDemo cenario, String nome, OffsetDateTime inicio, OffsetDateTime fim) {
    return eventos.criar(
        new EventoCreateRequest(
            nome,
            EVENTO_SINTETICO_PARA_DEMONSTRACAO,
            inicio,
            fim,
            CAMPUS_EXEMPLO,
            SAO_PAULO,
            SP,
            ORCAMENTO_EVENTO,
            PARTICIPANTES_ESPERADOS),
        cenario.administrador());
  }

  private void alterarStatus(Evento evento, StatusEvento status) {
    eventos.alterarStatus(
        evento.getId(),
        new EventoStatusUpdateRequest(status, null, evento.getVersion()),
        PerfilAcesso.ADMINISTRADOR);
    persistencia.flush();
  }

  record Eventos(Evento planejado, Evento inscricoes, Evento andamento, Evento concluido) {}
}
