package br.com.athletix.config.demo;

import static br.com.athletix.config.demo.DemoData.*;

import br.com.athletix.config.demo.EventosDemoSeeder.Eventos;
import br.com.athletix.evento.anexo.entity.TipoMaterialEvento;
import br.com.athletix.evento.anexo.service.AnexoEventoService;
import br.com.athletix.evento.cronograma.dto.AtividadeCronogramaCreateRequest;
import br.com.athletix.evento.cronograma.entity.StatusAtividadeCronograma;
import br.com.athletix.evento.cronograma.service.AtividadeCronogramaService;
import br.com.athletix.evento.entity.Evento;
import br.com.athletix.evento.responsabilidade.dto.ResponsabilidadeEventoCreateRequest;
import br.com.athletix.evento.responsabilidade.entity.AreaResponsabilidadeEvento;
import br.com.athletix.evento.responsabilidade.service.ResponsabilidadeEventoService;
import br.com.athletix.membro.entity.Membro;
import java.time.OffsetDateTime;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
class OrganizacaoEventosDemoSeeder {
  private final AtividadeCronogramaService cronograma;
  private final ResponsabilidadeEventoService responsabilidades;
  private final AnexoEventoService anexos;

  OrganizacaoEventosDemoSeeder(
      AtividadeCronogramaService cronograma,
      ResponsabilidadeEventoService responsabilidades,
      AnexoEventoService anexos) {
    this.cronograma = cronograma;
    this.responsabilidades = responsabilidades;
    this.anexos = anexos;
  }

  void criar(CenarioDemo cenario, Eventos eventos) {
    criarCronograma(eventos.andamento());
    for (Evento evento : new Evento[] {eventos.planejado(), eventos.inscricoes()}) {
      atividade(evento, REUNIAO_PREPARATORIA, 0, StatusAtividadeCronograma.AGENDADO);
      responsavel(evento, cenario.gestora(), AreaResponsabilidadeEvento.COORDENACAO_GERAL);
    }
    responsavel(
        eventos.andamento(), cenario.gestora(), AreaResponsabilidadeEvento.COORDENACAO_GERAL);
    responsavel(eventos.andamento(), cenario.tesoureiro(), AreaResponsabilidadeEvento.FINANCEIRO);
    responsavel(eventos.andamento(), cenario.associado(), AreaResponsabilidadeEvento.COMUNICACAO);
    anexos.criar(
        eventos.andamento().getId(),
        ArquivoSintetico.pdf(ARTE_EVENTO_PDF, ARTE_EVENTO),
        ARTE_EVENTO,
        DOCUMENTO_SINTETICO_SEM_DADOS_PESSOAIS,
        TipoMaterialEvento.ARTE,
        cenario.administrador());
    anexos.criar(
        eventos.andamento().getId(),
        ArquivoSintetico.pdf(REGULAMENTO_EVENTO_PDF, REGULAMENTO_DE_DEMONSTRACAO),
        REGULAMENTO_DE_DEMONSTRACAO,
        DOCUMENTO_SINTETICO_SEM_DADOS_PESSOAIS,
        TipoMaterialEvento.REGULAMENTO,
        cenario.administrador());
  }

  private void criarCronograma(Evento evento) {
    atividade(evento, RECEPCAO_PARTICIPANTES, 0, StatusAtividadeCronograma.CONCLUIDO);
    atividade(evento, ABERTURA_OFICIAL, 1, StatusAtividadeCronograma.CONCLUIDO);
    atividade(evento, PARTIDAS_CLASSIFICATORIAS, 2, StatusAtividadeCronograma.EM_ANDAMENTO);
    atividade(evento, CERIMONIA_PREMIACAO, 3, StatusAtividadeCronograma.AGENDADO);
  }

  private void atividade(
      Evento evento, String titulo, int ordem, StatusAtividadeCronograma status) {
    OffsetDateTime inicio =
        evento.getInicio().plusMinutes((long) ordem * INTERVALO_ATIVIDADES_MINUTOS);
    cronograma.criar(
        evento.getId(),
        new AtividadeCronogramaCreateRequest(
            titulo,
            EVENTO_SINTETICO_PARA_DEMONSTRACAO,
            inicio,
            inicio.plusMinutes(DURACAO_ATIVIDADE_MINUTOS),
            status,
            ordem));
  }

  private void responsavel(Evento evento, Membro membro, AreaResponsabilidadeEvento area) {
    responsabilidades.criar(
        evento.getId(), new ResponsabilidadeEventoCreateRequest(membro.getId(), area));
  }
}
