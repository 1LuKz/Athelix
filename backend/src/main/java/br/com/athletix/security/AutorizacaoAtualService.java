package br.com.athletix.security;

import br.com.athletix.auth.service.SessaoAtleticaService;
import br.com.athletix.cargo.entity.PerfilAcesso;
import br.com.athletix.membro.entity.Membro;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/** Fonte única do perfil atual para controllers: deriva da sessão, não do corpo da requisição. */
@Service
public class AutorizacaoAtualService {
    private final SessaoAtleticaService sessaoAtleticaService;

    public AutorizacaoAtualService(SessaoAtleticaService sessaoAtleticaService) {
        this.sessaoAtleticaService = sessaoAtleticaService;
    }

    public PerfilAcesso perfil(Authentication authentication, HttpServletRequest request) {
        return membro(authentication, request).getPerfilAcesso();
    }

    public void exigirAdministrador(Authentication authentication, HttpServletRequest request) {
        if (perfil(authentication, request) != PerfilAcesso.ADMINISTRADOR) {
            throw new AccessDeniedException("Somente administradores podem executar esta operação.");
        }
    }

    private Membro membro(Authentication authentication, HttpServletRequest request) {
        if (!(authentication != null && authentication.getPrincipal() instanceof AuthenticatedUserPrincipal principal)) {
            throw new AccessDeniedException("Sessão inválida.");
        }
        HttpSession session = request.getSession(false);
        Long membroId = session == null ? null
                : (Long) session.getAttribute(SessaoAtleticaService.MEMBRO_SELECIONADO_SESSION_ATTRIBUTE);
        if (membroId == null) {
            throw new AccessDeniedException("Selecione uma atlética para continuar.");
        }
        return sessaoAtleticaService.resolverSelecionado(principal.usuarioId(), membroId);
    }
}
