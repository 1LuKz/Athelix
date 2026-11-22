package br.com.athletix.config.demo;

import static br.com.athletix.config.demo.DemoData.*;

import br.com.athletix.config.demo.EventosDemoSeeder.Eventos;
import br.com.athletix.evento.entity.Evento;
import br.com.athletix.evento.participacao.dto.ParticipacaoEventoCreateRequest;
import br.com.athletix.evento.participacao.dto.ParticipacaoEventoStatusUpdateRequest;
import br.com.athletix.evento.participacao.entity.ParticipacaoEvento;
import br.com.athletix.evento.participacao.entity.StatusParticipacaoEvento;
import br.com.athletix.evento.participacao.service.ParticipacaoEventoService;
import br.com.athletix.membro.entity.Membro;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
class ParticipacoesDemoSeeder {
  private final ParticipacaoEventoService participacoes;
  private final EntityManager persistencia;

  ParticipacoesDemoSeeder(ParticipacaoEventoService participacoes, EntityManager persistencia) {
    this.participacoes = participacoes;
    this.persistencia = persistencia;
  }

  void criar(CenarioDemo cenario, Eventos eventos) {
    List<Membro> internos =
        List.of(
            cenario.administrador(), cenario.tesoureiro(), cenario.gestora(), cenario.associado());
    for (int indice = 0; indice < internos.size(); indice++) {
      ParticipacaoEvento participacao =
          participacoes.criar(
              eventos.andamento().getId(),
              new ParticipacaoEventoCreateRequest(internos.get(indice).getId(), null, null));
      aplicarStatus(eventos.andamento(), participacao, StatusParticipacaoEvento.values()[indice]);
    }
    for (int numero = 1; numero <= TOTAL_EXTERNOS_ANDAMENTO; numero++) {
      ParticipacaoEvento participacao = externo(eventos.andamento(), numero);
      aplicarStatus(
          eventos.andamento(),
          participacao,
          StatusParticipacaoEvento.values()[
              (numero - 1) % StatusParticipacaoEvento.values().length]);
    }
    for (int numero = 1; numero <= TOTAL_EXTERNOS_CONCLUIDO; numero++) {
      aplicarStatus(
          eventos.concluido(),
          externo(eventos.concluido(), numero),
          StatusParticipacaoEvento.PRESENTE);
    }
  }

  private ParticipacaoEvento externo(Evento evento, int numero) {
    return participacoes.criar(
        evento.getId(),
        new ParticipacaoEventoCreateRequest(
            null, PARTICIPANTE_EXTERNO + numero, EMAIL_EXTERNO.formatted(numero)));
  }

  private void aplicarStatus(
      Evento evento, ParticipacaoEvento participacao, StatusParticipacaoEvento status) {
    if (status == StatusParticipacaoEvento.INSCRITO) {
      return;
    }
    if (status == StatusParticipacaoEvento.PRESENTE) {
      alterarStatus(evento, participacao, StatusParticipacaoEvento.CONFIRMADO);
    }
    alterarStatus(evento, participacao, status);
  }

  private void alterarStatus(
      Evento evento, ParticipacaoEvento participacao, StatusParticipacaoEvento status) {
    participacoes.alterarStatus(
        evento.getId(),
        participacao.getId(),
        new ParticipacaoEventoStatusUpdateRequest(status, null, participacao.getVersion()));
    persistencia.flush();
  }
}
