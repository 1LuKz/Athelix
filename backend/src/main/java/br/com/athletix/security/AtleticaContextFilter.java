package br.com.athletix.security;

import br.com.athletix.auth.service.SessaoAtleticaService;
import br.com.athletix.auditoria.service.ContextoAtividade;
import br.com.athletix.shared.error.ApiErrorCode;
import br.com.athletix.shared.error.ApiErrorResponseWriter;
import br.com.athletix.shared.tenancy.ContextoAtletica;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** Resolve a atlética exclusivamente a partir do vínculo selecionado na sessão autenticada. */
public class AtleticaContextFilter extends OncePerRequestFilter {
    private final ContextoAtletica contextoAtletica;
    private final SessaoAtleticaService sessaoAtleticaService;
    private final ApiErrorResponseWriter errorWriter;
    private final ContextoAtividade contextoAtividade;

    public AtleticaContextFilter(ContextoAtletica contextoAtletica, SessaoAtleticaService sessaoAtleticaService,
                                 ApiErrorResponseWriter errorWriter, ContextoAtividade contextoAtividade) {
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
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof AuthenticatedUserPrincipal principal)) {
            chain.doFilter(request, response);
            return;
        }
        HttpSession session = request.getSession(false);
        Long membroId = session == null ? null
                : (Long) session.getAttribute(SessaoAtleticaService.MEMBRO_SELECIONADO_SESSION_ATTRIBUTE);
        if (membroId == null) {
            errorWriter.write(request, response, HttpStatus.FORBIDDEN, ApiErrorCode.FORBIDDEN,
                    "Selecione uma atlética ativa para acessar este recurso.");
            return;
        }
        final Long atleticaId;
        try {
            atleticaId = sessaoAtleticaService.resolverSelecionado(principal.usuarioId(), membroId).getAtleticaId();
        } catch (AccessDeniedException exception) {
            if (session != null) {
                session.removeAttribute(SessaoAtleticaService.MEMBRO_SELECIONADO_SESSION_ATTRIBUTE);
            }
            errorWriter.write(request, response, HttpStatus.FORBIDDEN, ApiErrorCode.FORBIDDEN,
                    "O vínculo selecionado não está mais disponível.");
            return;
        }
        try (ContextoAtletica.Escopo ignored = contextoAtletica.abrir(atleticaId);
             ContextoAtividade.Escopo atividade = contextoAtividade.abrir(atleticaId, principal.usuarioId(), membroId)) {
            chain.doFilter(request, response);
        }
    }
}
