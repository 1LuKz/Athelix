package br.com.athletix.tarefa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.atletica.repository.AtleticaRepository;
import br.com.athletix.auditoria.service.AtividadeUsuarioService;
import br.com.athletix.departamento.repository.DepartamentoRepository;
import br.com.athletix.evento.service.EventoService;
import br.com.athletix.gestao.repository.GestaoRepository;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.membro.repository.MembroRepository;
import br.com.athletix.shared.exception.ConcurrentModificationException;
import br.com.athletix.shared.tenancy.AtleticaResourceResolver;
import br.com.athletix.tarefa.dto.TarefaRequests;
import br.com.athletix.tarefa.dto.TarefaResponse;
import br.com.athletix.tarefa.entity.StatusTarefa;
import br.com.athletix.tarefa.entity.Tarefa;
import br.com.athletix.tarefa.repository.TarefaRepository;
import br.com.athletix.tarefa.service.TarefaAutorizacaoService;
import br.com.athletix.tarefa.service.TarefaService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TarefaServiceMockTest {

  @Mock private TarefaRepository tarefas;
  @Mock private EventoService eventos;
  @Mock private AtleticaRepository atleticas;
  @Mock private MembroRepository membros;
  @Mock private DepartamentoRepository departamentos;
  @Mock private GestaoRepository gestoes;
  @Mock private AtleticaResourceResolver resolver;
  @Mock private AtividadeUsuarioService auditoria;
  @Mock private TarefaAutorizacaoService autorizacao;
  @InjectMocks private TarefaService service;

  @Test
  void recusaAlteracaoDeStatusComVersaoDesatualizadaSemPersistir() {
    Tarefa tarefa = org.mockito.Mockito.mock(Tarefa.class);
    Membro autor = org.mockito.Mockito.mock(Membro.class);

    prepararCarregamento(tarefa);
    when(tarefa.getVersion()).thenReturn(4L);
    when(autorizacao.podeAtualizarStatus(autor, tarefa)).thenReturn(true);

    assertThrows(
        ConcurrentModificationException.class,
        () ->
            service.alterarStatus(
                10L, new TarefaRequests.Status(StatusTarefa.EM_ANDAMENTO, null, 3L), autor));

    verify(tarefas, never()).saveAndFlush(any(Tarefa.class));
    verify(auditoria, never()).registrar(any(), any(), any(), any());
    verify(tarefa, never()).status(any());
  }

  @Test
  void aplicaTransicaoValidaPersisteEAudita() {
    Tarefa tarefa = org.mockito.Mockito.mock(Tarefa.class);
    Membro autor = org.mockito.Mockito.mock(Membro.class);
    Atletica atletica = org.mockito.Mockito.mock(Atletica.class);

    prepararCarregamento(tarefa);
    when(resolver.atleticaIdAtual()).thenReturn(1L);
    when(tarefa.getId()).thenReturn(10L);
    when(tarefa.getVersion()).thenReturn(4L);
    when(tarefa.getEventoId()).thenReturn(3L);
    when(tarefa.getStatus()).thenReturn(StatusTarefa.A_FAZER);
    when(tarefa.atrasada(any())).thenReturn(false);
    when(autorizacao.podeAtualizarStatus(autor, tarefa)).thenReturn(true);
    when(tarefas.saveAndFlush(tarefa)).thenReturn(tarefa);
    when(atleticas.findByIdAndId(1L, 1L)).thenReturn(Optional.of(atletica));
    when(atletica.getFusoHorario()).thenReturn("America/Sao_Paulo");
    doAnswer(
            invocation -> {
              when(tarefa.getStatus()).thenReturn(invocation.getArgument(0));
              return null;
            })
        .when(tarefa)
        .status(any(StatusTarefa.class));

    TarefaResponse resposta =
        service.alterarStatus(
            10L, new TarefaRequests.Status(StatusTarefa.EM_ANDAMENTO, null, 4L), autor);

    assertEquals(StatusTarefa.EM_ANDAMENTO, resposta.status());
    verify(tarefa).status(StatusTarefa.EM_ANDAMENTO);
    verify(tarefas).saveAndFlush(tarefa);
    verify(auditoria)
        .registrar("TAREFA_STATUS_ATUALIZADO", "TAREFA", 10L, "Status: A_FAZER → EM_ANDAMENTO.");
  }

  private void prepararCarregamento(Tarefa tarefa) {
    when(resolver.requireByIdAndAtleticaId(eq(10L), any())).thenReturn(tarefa);
  }
}
