package br.com.athletix.financeiro.service;

import br.com.athletix.cargo.entity.PerfilAcesso;
import br.com.athletix.membro.entity.Membro;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

/** Operações financeiras detalhadas são exclusivas de administrador e financeiro. */
@Service
public class FinanceiroAutorizacaoService {
    public void exigirGerenciamentoFinanceiro(Membro membro) {
        PerfilAcesso perfil = membro.getPerfilAcesso();
        if (perfil != PerfilAcesso.ADMINISTRADOR && perfil != PerfilAcesso.FINANCEIRO) {
            throw new AccessDeniedException("Você não tem permissão para gerenciar lançamentos financeiros.");
        }
    }
}
