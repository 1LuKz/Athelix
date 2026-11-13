package br.com.athletix.evento.financeiro.service;

import br.com.athletix.atletica.repository.AtleticaRepository;
import br.com.athletix.evento.entity.Evento;
import br.com.athletix.evento.financeiro.dto.EventoFinanceiroResumoResponse;
import br.com.athletix.evento.financeiro.entity.SituacaoOrcamentoEvento;
import br.com.athletix.evento.service.EventoService;
import br.com.athletix.financeiro.entity.EstornoFinanceiro;
import br.com.athletix.financeiro.entity.LancamentoFinanceiro;
import br.com.athletix.financeiro.entity.StatusAprovacao;
import br.com.athletix.financeiro.entity.StatusLiquidacao;
import br.com.athletix.financeiro.entity.TipoLancamentoFinanceiro;
import br.com.athletix.financeiro.repository.EstornoFinanceiroRepository;
import br.com.athletix.financeiro.repository.LancamentoFinanceiroRepository;
import br.com.athletix.financeiro.service.FinanceiroAutorizacaoService;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.shared.money.MoneyValues;
import br.com.athletix.shared.time.FusoHorarioAtletica;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Resumo de orçamento por evento no regime de caixa e sem compensar receitas contra despesa realizada. */
@Service
public class EventoFinanceiroResumoService {
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(MoneyValues.SCALE);

    private final EventoService eventos;
    private final LancamentoFinanceiroRepository lancamentos;
    private final EstornoFinanceiroRepository estornos;
    private final AtleticaRepository atleticas;
    private final FinanceiroAutorizacaoService autorizacao;

    public EventoFinanceiroResumoService(EventoService eventos, LancamentoFinanceiroRepository lancamentos,
                                         EstornoFinanceiroRepository estornos, AtleticaRepository atleticas,
                                         FinanceiroAutorizacaoService autorizacao) {
        this.eventos = eventos;
        this.lancamentos = lancamentos;
        this.estornos = estornos;
        this.atleticas = atleticas;
        this.autorizacao = autorizacao;
    }

    @Transactional(readOnly = true)
    public EventoFinanceiroResumoResponse resumir(Long eventoId, LocalDate dataReferencia, Membro membro) {
        autorizacao.exigirConsultaFinanceira(membro);
        Evento evento = eventos.buscarDaAtleticaAtual(eventoId);
        LocalDate referencia = dataReferencia == null ? hoje(evento) : dataReferencia;
        List<LancamentoFinanceiro> doEvento = lancamentos.findAllByAtletica_IdAndEvento_Id(
                evento.getAtleticaId(), eventoId);
        Set<Long> ids = new HashSet<>();
        for (LancamentoFinanceiro lancamento : doEvento) ids.add(lancamento.getId());

        BigDecimal despesasPagas = ZERO;
        BigDecimal receitasRecebidas = ZERO;
        BigDecimal despesasAbertas = ZERO;
        for (LancamentoFinanceiro lancamento : doEvento) {
            if (lancamento.getStatusLiquidacao() == StatusLiquidacao.LIQUIDADO
                    && dataAte(lancamento.getDataLiquidacao(), referencia)) {
                if (lancamento.getTipo() == TipoLancamentoFinanceiro.DESPESA
                        && lancamento.getStatusAprovacao() == StatusAprovacao.APROVADA) {
                    despesasPagas = despesasPagas.add(lancamento.getValor());
                }
                if (lancamento.getTipo() == TipoLancamentoFinanceiro.RECEITA) {
                    receitasRecebidas = receitasRecebidas.add(lancamento.getValor());
                }
            }
            if (lancamento.getTipo() == TipoLancamentoFinanceiro.DESPESA
                    && lancamento.getStatusLiquidacao() == StatusLiquidacao.ABERTO
                    && lancamento.getStatusAprovacao() != StatusAprovacao.REJEITADA) {
                despesasAbertas = despesasAbertas.add(lancamento.getValor());
            }
        }

        BigDecimal estornosDespesa = ZERO;
        BigDecimal estornosReceita = ZERO;
        for (EstornoFinanceiro estorno : estornos.findAllByAtletica_Id(evento.getAtleticaId())) {
            if (!ids.contains(estorno.getLancamentoId()) || !dataAte(estorno.getDataEstorno(), referencia)) continue;
            if (estorno.getTipoLancamento() == TipoLancamentoFinanceiro.DESPESA) {
                estornosDespesa = estornosDespesa.add(estorno.getValorLancamento());
            } else {
                estornosReceita = estornosReceita.add(estorno.getValorLancamento());
            }
        }
        BigDecimal realizado = despesasPagas.subtract(estornosDespesa);
        BigDecimal receitas = receitasRecebidas.subtract(estornosReceita);
        BigDecimal previsto = evento.getOrcamentoPrevisto();
        BigDecimal saldo = previsto.subtract(realizado);
        BigDecimal percentual = previsto.signum() == 0 ? null : realizado.multiply(BigDecimal.valueOf(100))
                .divide(previsto, 2, MoneyValues.ROUNDING_MODE);
        SituacaoOrcamentoEvento situacao = previsto.signum() == 0 ? SituacaoOrcamentoEvento.SEM_ORCAMENTO
                : realizado.compareTo(previsto) > 0 ? SituacaoOrcamentoEvento.ESTOURADO
                : SituacaoOrcamentoEvento.DENTRO_DO_ORCAMENTO;
        return new EventoFinanceiroResumoResponse(eventoId, referencia, MoneyValues.format(previsto),
                MoneyValues.format(realizado), MoneyValues.format(saldo), percentual, situacao,
                MoneyValues.format(receitas), MoneyValues.format(despesasAbertas));
    }

    private boolean dataAte(LocalDate data, LocalDate referencia) {
        return data != null && !data.isAfter(referencia);
    }

    private LocalDate hoje(Evento evento) {
        return atleticas.findByIdAndId(evento.getAtleticaId(), evento.getAtleticaId())
                .map(atletica -> FusoHorarioAtletica.hoje(atletica.getFusoHorario()))
                .orElseGet(() -> FusoHorarioAtletica.hoje(evento.getFusoHorarioAtletica()));
    }
}
