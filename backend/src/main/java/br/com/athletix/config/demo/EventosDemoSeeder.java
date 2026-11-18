package br.com.athletix.config.demo;

import static br.com.athletix.config.demo.DemoData.*;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.evento.entity.Evento;
import br.com.athletix.evento.entity.StatusEvento;
import br.com.athletix.evento.repository.EventoRepository;
import br.com.athletix.gestao.entity.Gestao;
import br.com.athletix.membro.entity.Membro;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
class EventosDemoSeeder {
  private final EventoRepository eventoRepository;

  EventosDemoSeeder(EventoRepository eventoRepository) {
    this.eventoRepository = eventoRepository;
  }

  Eventos criar(
      Atletica engenharia,
      Gestao atual,
      Gestao anterior,
      Membro administradorMembro,
      Membro gestorMembro) {
    OffsetDateTime inicio = OffsetDateTime.now(ZoneId.of(FUSO_DEMONSTRACAO));
    Evento planejado =
        eventoRepository.save(
            evento(
                engenharia,
                atual,
                administradorMembro,
                TORNEIO_DE_INTEGRACAO,
                inicio.plusDays(INICIO_TORNEIO_DIAS),
                inicio.plusDays(FIM_TORNEIO_DIAS)));
    Evento inscricoes =
        eventoRepository.save(
            evento(
                engenharia,
                atual,
                gestorMembro,
                COPA_ABERTA,
                inicio.plusDays(INICIO_COPA_DIAS),
                inicio.plusDays(FIM_COPA_DIAS)));
    inscricoes.alterarStatus(StatusEvento.INSCRICOES_ABERTAS);
    eventoRepository.save(inscricoes);
    Evento andamento =
        eventoRepository.save(
            evento(
                engenharia,
                atual,
                gestorMembro,
                SEMANA_ESPORTIVA,
                inicio.minusHours(INICIO_SEMANA_ESPORTIVA_HORAS),
                inicio.plusDays(FIM_SEMANA_ESPORTIVA_DIAS)));
    andamento.alterarStatus(StatusEvento.EM_ANDAMENTO);
    eventoRepository.save(andamento);
    Evento concluido =
        eventoRepository.save(
            evento(
                engenharia,
                anterior,
                administradorMembro,
                JOGOS_DO_SEMESTRE,
                inicio.minusDays(INICIO_JOGOS_DIAS),
                inicio.minusDays(FIM_JOGOS_DIAS)));
    concluido.alterarStatus(StatusEvento.CONCLUIDO);
    eventoRepository.save(concluido);
    return new Eventos(planejado, inscricoes, andamento, concluido);
  }

  private Evento evento(
      Atletica atletica,
      Gestao gestao,
      Membro criador,
      String nome,
      OffsetDateTime inicio,
      OffsetDateTime fim) {
    return new Evento(
        atletica,
        gestao.getId(),
        nome,
        EVENTO_SINTETICO_PARA_DEMONSTRACAO,
        inicio,
        fim,
        CAMPUS_EXEMPLO,
        SAO_PAULO,
        SP,
        new BigDecimal(ORCAMENTO_EVENTO),
        PARTICIPANTES_ESPERADOS,
        criador.getId());
  }

  record Eventos(Evento planejado, Evento inscricoes, Evento andamento, Evento concluido) {}
}
