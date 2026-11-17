package br.com.athletix.patrimonio.service;

import br.com.athletix.cargo.entity.PerfilAcesso;
import br.com.athletix.membro.entity.Membro;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

/** Administração gerencia todo o patrimônio; gestor só atua sobre seu departamento principal. */
@Service
public class PatrimonioAutorizacaoService {
  public void exigirConsulta(Membro membro) {
    if (membro == null)
      throw new AccessDeniedException("Você não tem permissão para consultar patrimônio.");
  }

  public void exigirGerenciamento(Membro membro, Long departamentoId) {
    if (membro.getPerfilAcesso() == PerfilAcesso.ADMINISTRADOR) return;
    if (membro.getPerfilAcesso() == PerfilAcesso.GESTOR
        && membro.getDepartamentoId().equals(departamentoId)) return;
    throw new AccessDeniedException("Você não tem permissão para gerenciar este patrimônio.");
  }

  public void exigirAdministracao(Membro membro) {
    if (membro.getPerfilAcesso() != PerfilAcesso.ADMINISTRADOR) {
      throw new AccessDeniedException(
          "Somente a administração pode configurar categorias de patrimônio.");
    }
  }
}
