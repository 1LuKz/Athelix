package br.com.athletix.tarefa;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.athletix.auth.service.CapacidadeService;
import br.com.athletix.cargo.entity.PerfilAcesso;
import br.com.athletix.evento.entity.Evento;
import br.com.athletix.evento.responsabilidade.repository.ResponsabilidadeEventoRepository;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.tarefa.service.TarefaAutorizacaoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TarefaAutorizacaoServiceMockTest {

  @Mock private ResponsabilidadeEventoRepository responsabilidades;
  @Mock private CapacidadeService capacidades;
  @InjectMocks private TarefaAutorizacaoService service;

  @Test
  void financeiroCriadorSemDesignacaoNaoGerenciaTarefa() {
    Membro financeiro = membro(PerfilAcesso.FINANCEIRO, 7L, 1L);
    Evento evento = evento(20L);
    when(responsabilidades.existsByEvento_IdAndAtletica_IdAndMembro_IdAndAtiva(20L, 1L, 7L, true))
        .thenReturn(false);

    assertFalse(service.podeGerenciar(financeiro, evento, 10L));

    verify(responsabilidades)
        .existsByEvento_IdAndAtletica_IdAndMembro_IdAndAtiva(20L, 1L, 7L, true);
  }

  @Test
  void financeiroComResponsabilidadeAtivaGerenciaTarefa() {
    Membro financeiro = membro(PerfilAcesso.FINANCEIRO, 7L, 1L);
    Evento evento = evento(20L);
    when(responsabilidades.existsByEvento_IdAndAtletica_IdAndMembro_IdAndAtiva(20L, 1L, 7L, true))
        .thenReturn(true);

    assertTrue(service.podeGerenciar(financeiro, evento, 15L));
  }

  @Test
  void gestorGerenciaTarefaDoProprioDepartamentoMesmoSemResponsabilidadeNoEvento() {
    Membro gestor = membro(PerfilAcesso.GESTOR, 8L, 1L);
    Evento evento = evento(20L);
    when(gestor.getDepartamentoId()).thenReturn(10L);
    when(responsabilidades.existsByEvento_IdAndAtletica_IdAndMembro_IdAndAtiva(20L, 1L, 8L, true))
        .thenReturn(false);

    assertTrue(service.podeGerenciar(gestor, evento, 10L));
    assertFalse(service.podeGerenciar(gestor, evento, 11L));
  }

  private Membro membro(PerfilAcesso perfil, Long id, Long atleticaId) {
    Membro membro = org.mockito.Mockito.mock(Membro.class);
    when(membro.getPerfilAcesso()).thenReturn(perfil);
    when(membro.getId()).thenReturn(id);
    when(membro.getAtleticaId()).thenReturn(atleticaId);
    return membro;
  }

  private Evento evento(Long id) {
    Evento evento = org.mockito.Mockito.mock(Evento.class);
    when(evento.getId()).thenReturn(id);
    return evento;
  }
}
