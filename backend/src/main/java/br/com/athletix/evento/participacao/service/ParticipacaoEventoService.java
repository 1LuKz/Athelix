package br.com.athletix.evento.participacao.service;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.atletica.repository.AtleticaRepository;
import br.com.athletix.auditoria.service.AtividadeUsuarioService;
import br.com.athletix.evento.entity.Evento;
import br.com.athletix.evento.participacao.dto.ParticipacaoEventoCreateRequest;
import br.com.athletix.evento.participacao.dto.ParticipacaoEventoResumoResponse;
import br.com.athletix.evento.participacao.dto.ParticipacaoEventoStatusUpdateRequest;
import br.com.athletix.evento.participacao.entity.ParticipacaoEvento;
import br.com.athletix.evento.participacao.entity.StatusParticipacaoEvento;
import br.com.athletix.evento.participacao.repository.ParticipacaoEventoRepository;
import br.com.athletix.evento.service.EventoService;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.membro.entity.StatusMembro;
import br.com.athletix.membro.repository.MembroRepository;
import br.com.athletix.shared.exception.BusinessRuleException;
import br.com.athletix.shared.exception.ConcurrentModificationException;
import br.com.athletix.shared.exception.ConflictException;
import br.com.athletix.shared.exception.ResourceNotFoundException;
import br.com.athletix.shared.normalization.NomeNormalizado;
import br.com.athletix.shared.tenancy.AtleticaResourceResolver;
import br.com.athletix.usuario.service.EmailNormalizado;
import java.time.Instant;
import java.util.EnumSet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Inscrições e presença, sem transformar participantes externos em contas de usuário. */
@Service
public class ParticipacaoEventoService {
  private final ParticipacaoEventoRepository participacaoRepository;
  private final EventoService eventoService;
  private final AtleticaRepository atleticaRepository;
  private final MembroRepository membroRepository;
  private final AtleticaResourceResolver resourceResolver;
  private final AtividadeUsuarioService atividadeService;

  public ParticipacaoEventoService(
      ParticipacaoEventoRepository participacaoRepository,
      EventoService eventoService,
      AtleticaRepository atleticaRepository,
      MembroRepository membroRepository,
      AtleticaResourceResolver resourceResolver,
      AtividadeUsuarioService atividadeService) {
    this.participacaoRepository = participacaoRepository;
    this.eventoService = eventoService;
    this.atleticaRepository = atleticaRepository;
    this.membroRepository = membroRepository;
    this.resourceResolver = resourceResolver;
    this.atividadeService = atividadeService;
  }

  @Transactional
  public ParticipacaoEvento criar(Long eventoId, ParticipacaoEventoCreateRequest request) {
    Evento evento = eventoService.buscarDaAtleticaAtual(eventoId);
    Long atleticaId = resourceResolver.atleticaIdAtual();
    Atletica atletica =
        resourceResolver.requireByIdAndAtleticaId(atleticaId, atleticaRepository::findByIdAndId);
    ParticipacaoEvento participacao;
    if (request.membroId() != null) {
      Membro membro =
          resourceResolver.requireByIdAndAtleticaId(
              request.membroId(), membroRepository::findByIdAndAtletica_Id);
      if (membro.getStatus() != StatusMembro.ATIVO) {
        throw new BusinessRuleException("O participante interno deve ser um membro ativo.");
      }
      if (participacaoRepository.existsByEvento_IdAndMembro_Id(eventoId, membro.getId())) {
        throw new ConflictException("Este membro já participa do evento.");
      }
      participacao = new ParticipacaoEvento(atletica, evento, membro, null, null, Instant.now());
    } else {
      String email = EmailNormalizado.normalize(request.emailParticipante());
      if (participacaoRepository.existsByEvento_IdAndEmailParticipante(eventoId, email)) {
        throw new ConflictException("Já existe participante externo com este e-mail no evento.");
      }
      participacao =
          new ParticipacaoEvento(
              atletica,
              evento,
              null,
              NomeNormalizado.displayValue(request.nomeParticipante()),
              email,
              Instant.now());
    }
    ParticipacaoEvento salva = participacaoRepository.save(participacao);
    atividadeService.registrar(
        "PARTICIPACAO_EVENTO_CRIADA",
        "PARTICIPACAO_EVENTO",
        salva.getId(),
        "Participação criada no evento.");
    return salva;
  }

  @Transactional(readOnly = true)
  public Page<ParticipacaoEvento> listar(Long eventoId, Pageable pageable) {
    eventoService.buscarDaAtleticaAtual(eventoId);
    return participacaoRepository.findAllByEvento_IdAndAtletica_Id(
        eventoId, resourceResolver.atleticaIdAtual(), pageable);
  }

  @Transactional
  public ParticipacaoEvento alterarStatus(
      Long eventoId, Long participacaoId, ParticipacaoEventoStatusUpdateRequest request) {
    eventoService.buscarDaAtleticaAtual(eventoId);
    ParticipacaoEvento participacao =
        participacaoRepository
            .findByIdAndEvento_IdAndAtletica_Id(
                participacaoId, eventoId, resourceResolver.atleticaIdAtual())
            .orElseThrow(ResourceNotFoundException::new);
    if (!participacao.getVersion().equals(request.version())) {
      throw new ConcurrentModificationException();
    }
    StatusParticipacaoEvento anterior = participacao.getStatus();
    if (anterior == request.status()) {
      throw new BusinessRuleException("A participação já está neste status.");
    }
    Instant agora = Instant.now();
    if (request.status() == StatusParticipacaoEvento.CANCELADO) {
      participacao.cancelar();
    } else if (anterior == StatusParticipacaoEvento.INSCRITO
        && request.status() == StatusParticipacaoEvento.CONFIRMADO) {
      participacao.confirmar(agora);
    } else if (anterior == StatusParticipacaoEvento.CONFIRMADO
        && request.status() == StatusParticipacaoEvento.PRESENTE) {
      participacao.registrarPresenca(agora);
    } else if (anterior == StatusParticipacaoEvento.PRESENTE
        && request.status() == StatusParticipacaoEvento.CONFIRMADO) {
      String motivo = NomeNormalizado.optionalDisplayValue(request.motivoCorrecao());
      if (motivo == null) {
        throw new BusinessRuleException("Informe o motivo da correção de presença.");
      }
      participacao.corrigirPresenca(agora);
      atividadeService.registrar(
          "PRESENCA_EVENTO_CORRIGIDA",
          "PARTICIPACAO_EVENTO",
          participacao.getId(),
          resumirMotivo(motivo));
      return participacao;
    } else {
      throw new BusinessRuleException("Transição de status inválida para esta participação.");
    }
    atividadeService.registrar(
        "STATUS_PARTICIPACAO_EVENTO_ATUALIZADO",
        "PARTICIPACAO_EVENTO",
        participacao.getId(),
        "Status da participação alterado: " + anterior + " para " + request.status() + ".");
    return participacao;
  }

  @Transactional(readOnly = true)
  public ParticipacaoEventoResumoResponse resumo(Long eventoId) {
    Evento evento = eventoService.buscarDaAtleticaAtual(eventoId);
    long inscritos =
        participacaoRepository.countByEvento_IdAndStatusIn(
            eventoId,
            EnumSet.of(
                StatusParticipacaoEvento.INSCRITO,
                StatusParticipacaoEvento.CONFIRMADO,
                StatusParticipacaoEvento.PRESENTE));
    long confirmados =
        participacaoRepository.countByEvento_IdAndStatusIn(
            eventoId,
            EnumSet.of(StatusParticipacaoEvento.CONFIRMADO, StatusParticipacaoEvento.PRESENTE));
    long presentes =
        participacaoRepository.countByEvento_IdAndStatusIn(
            eventoId, EnumSet.of(StatusParticipacaoEvento.PRESENTE));
    return new ParticipacaoEventoResumoResponse(
        eventoId, evento.getParticipantesEsperados(), inscritos, confirmados, presentes);
  }

  private String resumirMotivo(String motivo) {
    String resumo = "Presença corrigida. Motivo: " + motivo;
    return resumo.length() <= 500 ? resumo : resumo.substring(0, 500);
  }
}
