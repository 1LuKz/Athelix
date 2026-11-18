package br.com.athletix.security;

import br.com.athletix.auditoria.service.ContextoAtividade;
import br.com.athletix.auth.service.SessaoAtleticaService;
import br.com.athletix.shared.error.ApiErrorCode;
import br.com.athletix.shared.error.ApiErrorResponseWriter;
import br.com.athletix.shared.tenancy.ContextoAtletica;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/** Resolve a atlética exclusivamente a partir do vínculo selecionado na sessão autenticada. */
public class AtleticaContextFilter extends OncePerRequestFilter {
  private final ContextoAtletica contextoAtletica;
  private final SessaoAtleticaService sessaoAtleticaService;
  private final ApiErrorResponseWriter errorWriter;
  private final ContextoAtividade contextoAtividade;

  public AtleticaContextFilter(
      ContextoAtletica contextoAtletica,
      SessaoAtleticaService sessaoAtleticaService,
      ApiErrorResponseWriter errorWriter,
      ContextoAtividade contextoAtividade) {
    this.contextoAtletica = contextoAtletica;
    this.sessaoAtleticaService = sessaoAtleticaService;
    this.errorWriter = errorWriter;
    this.contextoAtividade = contextoAtividade;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI().substring(request.getContextPath().length());
    return path.startsWith("/auth/") || path.startsWith("/h2-console/");
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    AuthenticatedUserPrincipal principal = usuarioAutenticado();
    if (principal == null) {
      chain.doFilter(request, response);
      return;
    }
    HttpSession session = request.getSession(false);
    Long membroId = membroSelecionado(session);
    if (membroId == null) {
      escreverErroSemAtleticaSelecionada(request, response);
      return;
    }
    Long atleticaId;
    try {
      atleticaId = resolverAtleticaSelecionada(principal, membroId);
    } catch (AccessDeniedException exception) {
      removerMembroSelecionado(session);
      escreverErroVinculoIndisponivel(request, response);
      return;
    }
    filtrarNoContextoAtletica(request, response, chain, principal, membroId, atleticaId);
  }

  private AuthenticatedUserPrincipal usuarioAutenticado() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null
        && authentication.isAuthenticated()
        && authentication.getPrincipal() instanceof AuthenticatedUserPrincipal principal) {
      return principal;
    }
    return null;
  }

  private Long membroSelecionado(HttpSession session) {
    return session == null
        ? null
        : (Long) session.getAttribute(SessaoAtleticaService.MEMBRO_SELECIONADO_SESSION_ATTRIBUTE);
  }

  private void escreverErroSemAtleticaSelecionada(
      HttpServletRequest request, HttpServletResponse response) throws IOException {
    errorWriter.write(
        request,
        response,
        HttpStatus.FORBIDDEN,
        ApiErrorCode.FORBIDDEN,
        "Selecione uma atlética ativa para acessar este recurso.");
  }

  private Long resolverAtleticaSelecionada(AuthenticatedUserPrincipal principal, Long membroId) {
    return sessaoAtleticaService
        .resolverSelecionado(principal.usuarioId(), membroId)
        .getAtleticaId();
  }

  private void filtrarNoContextoAtletica(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain chain,
      AuthenticatedUserPrincipal principal,
      Long membroId,
      Long atleticaId)
      throws ServletException, IOException {
    try (ContextoAtletica.Escopo ignored = contextoAtletica.abrir(atleticaId);
        ContextoAtividade.Escopo atividade =
            contextoAtividade.abrir(atleticaId, principal.usuarioId(), membroId)) {
      chain.doFilter(request, response);
    }
  }

  private void removerMembroSelecionado(HttpSession session) {
    if (session != null) {
      session.removeAttribute(SessaoAtleticaService.MEMBRO_SELECIONADO_SESSION_ATTRIBUTE);
    }
  }

  private void escreverErroVinculoIndisponivel(
      HttpServletRequest request, HttpServletResponse response) throws IOException {
    errorWriter.write(
        request,
        response,
        HttpStatus.FORBIDDEN,
        ApiErrorCode.FORBIDDEN,
        "O vínculo selecionado não está mais disponível.");
  }
}
