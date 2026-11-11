package br.com.athletix.notificacao.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.atletica.repository.AtleticaRepository;
import br.com.athletix.notificacao.entity.Notificacao;
import br.com.athletix.notificacao.entity.TipoNotificacao;
import br.com.athletix.notificacao.repository.NotificacaoRepository;
import br.com.athletix.shared.tenancy.ContextoAtletica;
import br.com.athletix.shared.time.FusoHorarioAtletica;
import br.com.athletix.tarefa.entity.Tarefa;
import br.com.athletix.tarefa.repository.TarefaRepository;

@Service
public class NotificacaoPrazoService {
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final AtleticaRepository atleticas;
    private final TarefaRepository tarefas;
    private final NotificacaoRepository notificacoes;
    private final ContextoAtletica contexto;

    public NotificacaoPrazoService(AtleticaRepository atleticas, TarefaRepository tarefas,
                                   NotificacaoRepository notificacoes, ContextoAtletica contexto) {
        this.atleticas = atleticas;
        this.tarefas = tarefas;
        this.notificacoes = notificacoes;
        this.contexto = contexto;
    }

    /** Gera os avisos de uma atlética usando o "hoje" do fuso dela. Retorna quantos foram criados. */
    @Transactional
    public int gerarParaAtletica(Long atleticaId, Instant agora) {
        Atletica atletica = atleticas.findById(atleticaId).filter(Atletica::isAtiva).orElse(null);
        if (atletica == null) {
            return 0;
        }
        LocalDate hoje = FusoHorarioAtletica.dataDe(agora, atletica.getFusoHorario());
        int criadas = 0;
        try (ContextoAtletica.Escopo ignored = contexto.abrir(atleticaId)) {
            for (Tarefa t : tarefas.findNotificaveis(atleticaId, hoje.plusDays(1))) {
                TipoNotificacao tipo = tipoPara(t.getPrazo(), hoje);
                if (tipo == null) continue;
                Long destinatario = t.getResponsavelMembroId(); // sempre o responsável atual
                if (notificacoes.existsByDestinatarioMembroIdAndTarefaIdAndTipoAndDataReferencia(
                        destinatario, t.getId(), tipo, hoje)) continue;
                notificacoes.save(montar(t, tipo, hoje, destinatario));
                criadas++;
            }
        }
        return criadas;
    }

    private static TipoNotificacao tipoPara(LocalDate prazo, LocalDate hoje) {
        if (prazo.isBefore(hoje)) return TipoNotificacao.ATRASADA;
        if (prazo.equals(hoje.plusDays(1))) return TipoNotificacao.PRAZO_PROXIMO;
        return null; // vence hoje ou mais adiante: sem aviso nesta versão
    }

    private Notificacao montar(Tarefa t, TipoNotificacao tipo, LocalDate hoje, Long destinatario) {
        String evento = t.getEvento().getNome();
        String prazo = t.getPrazo().format(DATA);
        boolean atrasada = tipo == TipoNotificacao.ATRASADA;
        String titulo = (atrasada ? "Tarefa atrasada: " : "Prazo amanhã: ") + t.getTitulo();
        String mensagem = "A tarefa \"" + t.getTitulo() + "\" do evento \"" + evento + "\" "
                + (atrasada ? "venceu em " + prazo + " e ainda não foi concluída."
                : "vence amanhã (" + prazo + ").");
        return new Notificacao(t.getAtleticaId(), destinatario, t.getId(), tipo, hoje,
                limitar(titulo, 160), limitar(mensagem, 500));
    }

    private static String limitar(String texto, int max) {
        return texto.length() <= max ? texto : texto.substring(0, max - 3) + "...";
    }
}