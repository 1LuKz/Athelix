package br.com.athletix.auth.service;

import br.com.athletix.security.AuthenticatedUserPrincipal;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;

/** Invalida sessões registradas da conta após uma redefinição de senha. */
@Service
public class InvalidacaoSessaoUsuarioService {
    private final SessionRegistry sessionRegistry;

    public InvalidacaoSessaoUsuarioService(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    public void invalidarSessoesAnteriores(Long usuarioId) {
        invalidarSessoesExceto(usuarioId, null);
    }

    public void invalidarSessoesExceto(Long usuarioId, String sessionIdMantida) {
        sessionRegistry.getAllPrincipals().stream()
                .filter(AuthenticatedUserPrincipal.class::isInstance)
                .map(AuthenticatedUserPrincipal.class::cast)
                .filter(principal -> principal.usuarioId().equals(usuarioId))
                .forEach(principal -> sessionRegistry.getAllSessions(principal, false).stream()
                        .filter(session -> sessionIdMantida == null || !sessionIdMantida.equals(session.getSessionId()))
                        .forEach(SessionInformation::expireNow));
    }
}
