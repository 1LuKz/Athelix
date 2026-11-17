package br.com.athletix.tarefa.service;

import br.com.athletix.auth.service.Capacidade;
import br.com.athletix.auth.service.CapacidadeService;
import br.com.athletix.cargo.entity.PerfilAcesso;
import br.com.athletix.evento.entity.Evento;
import br.com.athletix.evento.responsabilidade.repository.ResponsabilidadeEventoRepository;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.tarefa.entity.Tarefa;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class TarefaAutorizacaoService {
  private final ResponsabilidadeEventoRepository responsabilidades;
  private final CapacidadeService capacidades;

  public TarefaAutorizacaoService(
      ResponsabilidadeEventoRepository responsabilidades, CapacidadeService capacidades) {
    this.responsabilidades = responsabilidades;
    this.capacidades = capacidades;
  }

  public boolean podeGerenciar(Membro membro, Evento evento, Long departamentoId) {
    PerfilAcesso perfil = membro.getPerfilAcesso();
    if (perfil == PerfilAcesso.ADMINISTRADOR) return true;
    if (perfil == PerfilAcesso.MEMBRO) return false;
    boolean responsavelDoEvento =
        responsabilidades.existsByEvento_IdAndAtletica_IdAndMembro_IdAndAtiva(
            evento.getId(), membro.getAtleticaId(), membro.getId(), true);
    if (perfil == PerfilAcesso.FINANCEIRO) return responsavelDoEvento;
    return responsavelDoEvento
        || (departamentoId != null && departamentoId.equals(membro.getDepartamentoId()));
  }

  public void exigirGerenciar(Membro membro, Evento evento, Long departamentoId) {
    if (!podeGerenciar(membro, evento, departamentoId)) {
      throw new AccessDeniedException(
          "Você não tem permissão para gerenciar tarefas deste evento.");
    }
  }

  public boolean ehResponsavel(Membro membro, Tarefa tarefa) {
    return membro.getId().equals(tarefa.getResponsavelMembroId());
  }

  public boolean podeAtualizarStatus(Membro membro, Tarefa tarefa) {
    return podeGerenciar(membro, tarefa.getEvento(), tarefa.getDepartamentoId())
        || (ehResponsavel(membro, tarefa)
            && capacidades
                .para(membro.getPerfilAcesso())
                .contains(Capacidade.ATUALIZAR_PROPRIA_TAREFA));
  }

  /** Somente MEMBRO fica restrito às próprias tarefas na consulta. */
  public boolean veTodas(Membro membro) {
    return membro.getPerfilAcesso() != PerfilAcesso.MEMBRO;
  }
}
