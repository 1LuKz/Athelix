package br.com.athletix.evento.service;

import br.com.athletix.cargo.entity.PerfilAcesso;
import br.com.athletix.evento.entity.Evento;
import br.com.athletix.evento.responsabilidade.repository.ResponsabilidadeEventoRepository;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.security.AutorizacaoAtualService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/** Enquanto não há equipe de organização, o membro criador é o organizador do evento. */
@Service
public class OrganizacaoEventoAuthorizationService {
  private final EventoService eventoService;
  private final AutorizacaoAtualService autorizacao;
  private final ResponsabilidadeEventoRepository responsabilidadeRepository;

  public OrganizacaoEventoAuthorizationService(
      EventoService eventoService,
      AutorizacaoAtualService autorizacao,
      ResponsabilidadeEventoRepository responsabilidadeRepository) {
    this.eventoService = eventoService;
    this.autorizacao = autorizacao;
    this.responsabilidadeRepository = responsabilidadeRepository;
  }

  public void exigirOrganizadorOuAdministrador(
      Long eventoId, Authentication authentication, HttpServletRequest request) {
    Membro membro = autorizacao.membroAtual(authentication, request);
    Evento evento = eventoService.buscarDaAtleticaAtual(eventoId);
    if (membro.getPerfilAcesso() != PerfilAcesso.ADMINISTRADOR
        && !membro.getId().equals(evento.getCriadoPorMembroId())
        && !responsabilidadeRepository.existsByEvento_IdAndAtletica_IdAndMembro_IdAndAtiva(
            eventoId, membro.getAtleticaId(), membro.getId(), true)) {
      throw new AccessDeniedException(
          "Apenas organizadores ou administradores podem acessar participantes.");
    }
  }
}
