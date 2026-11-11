package br.com.athletix.notificacao;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.atletica.repository.AtleticaRepository;
import br.com.athletix.evento.entity.Evento;
import br.com.athletix.notificacao.entity.Notificacao;
import br.com.athletix.notificacao.entity.TipoNotificacao;
import br.com.athletix.notificacao.repository.NotificacaoRepository;
import br.com.athletix.notificacao.service.NotificacaoPrazoService;
import br.com.athletix.shared.tenancy.ContextoAtletica;
import br.com.athletix.tarefa.entity.Tarefa;
import br.com.athletix.tarefa.repository.TarefaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacaoPrazoServiceMockTest {

    @Mock private AtleticaRepository atleticas;
    @Mock private TarefaRepository tarefas;
    @Mock private NotificacaoRepository notificacoes;
    @Mock private ContextoAtletica contexto;
    @Mock private ContextoAtletica.Escopo escopo;
    @InjectMocks private NotificacaoPrazoService service;

    @Test
    void criaAvisoDeVesperaNoFusoDaAtletica() {
        Atletica atletica = ativa("America/Sao_Paulo");
        Tarefa tarefa = tarefaComPrazo(LocalDate.of(2027, 6, 10));
        Evento evento = org.mockito.Mockito.mock(Evento.class);
        when(tarefa.getTitulo()).thenReturn("Prestar contas");
        when(tarefa.getEvento()).thenReturn(evento);
        when(evento.getNome()).thenReturn("Festival");
        Instant agora = Instant.parse("2027-06-10T02:30:00Z"); // 09/06 às 23:30 em São Paulo

        when(atleticas.findById(1L)).thenReturn(Optional.of(atletica));
        when(contexto.abrir(1L)).thenReturn(escopo);
        when(tarefas.findNotificaveis(1L, LocalDate.of(2027, 6, 10))).thenReturn(List.of(tarefa));
        when(notificacoes.existsByDestinatarioMembroIdAndTarefaIdAndTipoAndDataReferencia(
                7L, 9L, TipoNotificacao.PRAZO_PROXIMO, LocalDate.of(2027, 6, 9))).thenReturn(false);

        assertEquals(1, service.gerarParaAtletica(1L, agora));

        ArgumentCaptor<Notificacao> captor = ArgumentCaptor.forClass(Notificacao.class);
        verify(notificacoes).save(captor.capture());
        Notificacao aviso = captor.getValue();
        assertEquals(TipoNotificacao.PRAZO_PROXIMO, aviso.getTipo());
        assertEquals(LocalDate.of(2027, 6, 9), aviso.getDataReferencia());
        assertEquals(7L, aviso.getDestinatarioMembroId());
        assertEquals(9L, aviso.getTarefaId());
        verify(escopo).close();
    }

    @Test
    void naoDuplicaAvisoJaGeradoParaOMesmoDia() {
        Atletica atletica = ativa("America/Sao_Paulo");
        Tarefa tarefa = tarefaComPrazo(LocalDate.of(2027, 6, 11));
        Instant agora = Instant.parse("2027-06-10T15:00:00Z");

        when(atleticas.findById(1L)).thenReturn(Optional.of(atletica));
        when(contexto.abrir(1L)).thenReturn(escopo);
        when(tarefas.findNotificaveis(1L, LocalDate.of(2027, 6, 11))).thenReturn(List.of(tarefa));
        when(notificacoes.existsByDestinatarioMembroIdAndTarefaIdAndTipoAndDataReferencia(
                7L, 9L, TipoNotificacao.PRAZO_PROXIMO, LocalDate.of(2027, 6, 10))).thenReturn(true);

        assertEquals(0, service.gerarParaAtletica(1L, agora));

        verify(notificacoes, never()).save(any(Notificacao.class));
        verify(escopo).close();
    }

    @Test
    void ignoraAtleticaInativaSemAbrirContexto() {
        Atletica atletica = org.mockito.Mockito.mock(Atletica.class);
        when(atletica.isAtiva()).thenReturn(false);
        when(atleticas.findById(1L)).thenReturn(Optional.of(atletica));

        assertEquals(0, service.gerarParaAtletica(1L, Instant.parse("2027-06-10T15:00:00Z")));

        verify(contexto, never()).abrir(any());
        verify(tarefas, never()).findNotificaveis(any(), any());
    }

    private Atletica ativa(String fuso) {
        Atletica atletica = org.mockito.Mockito.mock(Atletica.class);
        when(atletica.isAtiva()).thenReturn(true);
        when(atletica.getFusoHorario()).thenReturn(fuso);
        return atletica;
    }

    private Tarefa tarefaComPrazo(LocalDate prazo) {
        Tarefa tarefa = org.mockito.Mockito.mock(Tarefa.class);
        when(tarefa.getId()).thenReturn(9L);
        when(tarefa.getResponsavelMembroId()).thenReturn(7L);
        when(tarefa.getPrazo()).thenReturn(prazo);
        return tarefa;
    }
}
