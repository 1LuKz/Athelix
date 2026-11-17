package br.com.athletix.evento.cronograma.service;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.atletica.repository.AtleticaRepository;
import br.com.athletix.auditoria.service.AtividadeUsuarioService;
import br.com.athletix.evento.cronograma.dto.AtividadeCronogramaCreateRequest;
import br.com.athletix.evento.cronograma.dto.AtividadeCronogramaUpdateRequest;
import br.com.athletix.evento.cronograma.entity.AtividadeCronograma;
import br.com.athletix.evento.cronograma.entity.StatusAtividadeCronograma;
import br.com.athletix.evento.cronograma.repository.AtividadeCronogramaRepository;
import br.com.athletix.evento.entity.Evento;
import br.com.athletix.evento.service.EventoService;
import br.com.athletix.shared.exception.BusinessRuleException;
import br.com.athletix.shared.exception.ConcurrentModificationException;
import br.com.athletix.shared.exception.ResourceNotFoundException;
import br.com.athletix.shared.normalization.NomeNormalizado;
import br.com.athletix.shared.tenancy.AtleticaResourceResolver;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AtividadeCronogramaService {
  private final AtividadeCronogramaRepository atividadeRepository;
  private final EventoService eventoService;
  private final AtleticaRepository atleticaRepository;
  private final AtleticaResourceResolver resourceResolver;
  private final AtividadeUsuarioService atividadeUsuarioService;

  public AtividadeCronogramaService(
      AtividadeCronogramaRepository atividadeRepository,
      EventoService eventoService,
      AtleticaRepository atleticaRepository,
      AtleticaResourceResolver resourceResolver,
      AtividadeUsuarioService atividadeUsuarioService) {
    this.atividadeRepository = atividadeRepository;
    this.eventoService = eventoService;
    this.atleticaRepository = atleticaRepository;
    this.resourceResolver = resourceResolver;
    this.atividadeUsuarioService = atividadeUsuarioService;
  }

  @Transactional(readOnly = true)
  public List<AtividadeCronograma> listar(Long eventoId) {
    eventoService.buscarDaAtleticaAtual(eventoId);
    return atividadeRepository.findAllByEvento_IdAndAtletica_IdOrderByInicioAscOrdemAscIdAsc(
        eventoId, resourceResolver.atleticaIdAtual());
  }

  @Transactional
  public AtividadeCronograma criar(Long eventoId, AtividadeCronogramaCreateRequest request) {
    Evento evento = eventoService.buscarDaAtleticaAtual(eventoId);
    validarPeriodo(evento, request.inicio(), request.fim());
    Long atleticaId = resourceResolver.atleticaIdAtual();
    Atletica atletica =
        resourceResolver.requireByIdAndAtleticaId(atleticaId, atleticaRepository::findByIdAndId);
    AtividadeCronograma atividade =
        atividadeRepository.save(
            new AtividadeCronograma(
                atletica,
                evento,
                NomeNormalizado.displayValue(request.titulo()),
                NomeNormalizado.optionalDisplayValue(request.descricao()),
                request.inicio(),
                request.fim(),
                request.status() == null ? StatusAtividadeCronograma.AGENDADO : request.status(),
                request.ordem()));
    atividadeUsuarioService.registrar(
        "ATIVIDADE_CRONOGRAMA_CRIADA",
        "ATIVIDADE_CRONOGRAMA",
        atividade.getId(),
        "Atividade de cronograma criada.");
    return atividade;
  }

  @Transactional
  public AtividadeCronograma atualizar(
      Long eventoId, Long atividadeId, AtividadeCronogramaUpdateRequest request) {
    Evento evento = eventoService.buscarDaAtleticaAtual(eventoId);
    AtividadeCronograma atividade = buscar(eventoId, atividadeId);
    assertVersion(atividade, request.version());
    validarPeriodo(evento, request.inicio(), request.fim());
    atividade.atualizar(
        NomeNormalizado.displayValue(request.titulo()),
        NomeNormalizado.optionalDisplayValue(request.descricao()),
        request.inicio(),
        request.fim(),
        request.status(),
        request.ordem());
    atividadeUsuarioService.registrar(
        "ATIVIDADE_CRONOGRAMA_ATUALIZADA",
        "ATIVIDADE_CRONOGRAMA",
        atividade.getId(),
        "Atividade de cronograma atualizada.");
    return atividade;
  }

  @Transactional
  public void remover(Long eventoId, Long atividadeId, Long version) {
    eventoService.buscarDaAtleticaAtual(eventoId);
    AtividadeCronograma atividade = buscar(eventoId, atividadeId);
    assertVersion(atividade, version);
    atividadeRepository.delete(atividade);
    atividadeUsuarioService.registrar(
        "ATIVIDADE_CRONOGRAMA_REMOVIDA",
        "ATIVIDADE_CRONOGRAMA",
        atividadeId,
        "Atividade de cronograma removida.");
  }

  private AtividadeCronograma buscar(Long eventoId, Long atividadeId) {
    return atividadeRepository
        .findByIdAndEvento_IdAndAtletica_Id(
            atividadeId, eventoId, resourceResolver.atleticaIdAtual())
        .orElseThrow(ResourceNotFoundException::new);
  }

  private void validarPeriodo(Evento evento, OffsetDateTime inicio, OffsetDateTime fim) {
    if (inicio == null
        || inicio.isBefore(evento.getInicio())
        || inicio.isAfter(evento.getFim())
        || (fim != null && (fim.isBefore(inicio) || fim.isAfter(evento.getFim())))) {
      throw new BusinessRuleException(
          "As datas da atividade devem estar dentro do período do evento.");
    }
  }

  private void assertVersion(AtividadeCronograma atividade, Long version) {
    if (!atividade.getVersion().equals(version)) {
      throw new ConcurrentModificationException();
    }
  }
}
