package br.com.athletix.financeiro.service;

import br.com.athletix.atletica.repository.AtleticaRepository;
import br.com.athletix.financeiro.dto.ContaAbertaResponse;
import br.com.athletix.financeiro.dto.EvolucaoMensalResponse;
import br.com.athletix.financeiro.dto.FinanceiroResumoResponse;
import br.com.athletix.financeiro.entity.EstornoFinanceiro;
import br.com.athletix.financeiro.entity.LancamentoFinanceiro;
import br.com.athletix.financeiro.entity.StatusLiquidacao;
import br.com.athletix.financeiro.entity.StatusAprovacao;
import br.com.athletix.financeiro.entity.TipoLancamentoFinanceiro;
import br.com.athletix.financeiro.repository.EstornoFinanceiroRepository;
import br.com.athletix.financeiro.repository.LancamentoFinanceiroRepository;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.shared.exception.BusinessRuleException;
import br.com.athletix.shared.money.MoneyValues;
import br.com.athletix.shared.pagination.PageResponse;
import br.com.athletix.shared.tenancy.AtleticaResourceResolver;
import br.com.athletix.shared.time.FusoHorarioAtletica;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/** Consultas por regime de caixa. Valores do relatório são calculados, jamais persistidos. */
@Service
public class FinanceiroConsultaService {
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(MoneyValues.SCALE);

    private final LancamentoFinanceiroRepository lancamentos;
    private final EstornoFinanceiroRepository estornos;
    private final AtleticaRepository atleticas;
    private final AtleticaResourceResolver resolver;
    private final FinanceiroAutorizacaoService autorizacao;

    public FinanceiroConsultaService(LancamentoFinanceiroRepository lancamentos, EstornoFinanceiroRepository estornos,
                                     AtleticaRepository atleticas, AtleticaResourceResolver resolver,
                                     FinanceiroAutorizacaoService autorizacao) {
        this.lancamentos = lancamentos;
        this.estornos = estornos;
        this.atleticas = atleticas;
        this.resolver = resolver;
        this.autorizacao = autorizacao;
    }

    @Transactional(readOnly = true)
    public FinanceiroResumoResponse resumo(LocalDate dataReferencia, Membro membro) {
        autorizacao.exigirConsultaFinanceira(membro);
        LocalDate referencia = dataReferencia == null ? hoje() : dataReferencia;
        Movimento movimento = movimentoAte(referencia);
        Pendencias pendencias = pendenciasAtuais(horaLocal());
        return FinanceiroResumoResponse.of(referencia, ZERO, movimento.recebimentos, movimento.pagamentos,
                movimento.estornosReceitas, movimento.estornosDespesas, movimento.saldo(),
                pendencias.receber, pendencias.pagar, pendencias.vencidas);
    }

    @Transactional(readOnly = true)
    public List<EvolucaoMensalResponse> evolucaoMensal(YearMonth mesDe, YearMonth mesAte, Membro membro) {
        autorizacao.exigirConsultaFinanceira(membro);
        if (mesDe == null || mesAte == null) {
            throw new BusinessRuleException("Os meses inicial e final são obrigatórios.");
        }
        if (mesAte.isBefore(mesDe)) {
            throw new BusinessRuleException("O mês final não pode ser anterior ao mês inicial.");
        }
        if (mesDe.plusMonths(119).isBefore(mesAte)) {
            throw new BusinessRuleException("O intervalo mensal não pode exceder 120 meses.");
        }
        List<EvolucaoMensalResponse> resultado = new ArrayList<>();
        for (YearMonth mes = mesDe; !mes.isAfter(mesAte); mes = mes.plusMonths(1)) {
            Movimento movimento = movimentoNoPeriodo(mes.atDay(1), mes.atEndOfMonth());
            resultado.add(EvolucaoMensalResponse.of(mes,
                    movimento.recebimentos.subtract(movimento.estornosReceitas),
                    movimento.pagamentos.subtract(movimento.estornosDespesas),
                    movimento.estornosReceitas, movimento.estornosDespesas));
        }
        return resultado;
    }

    @Transactional(readOnly = true)
    public PageResponse<ContaAbertaResponse> contas(Pageable pageable, Membro membro) {
        autorizacao.exigirConsultaFinanceira(membro);
        LocalDate hoje = hoje();
        Long atleticaId = resolver.atleticaIdAtual();
        return PageResponse.from(lancamentos.findAllByAtletica_IdAndStatusLiquidacaoAndStatusAprovacaoNot(atleticaId,
                        StatusLiquidacao.ABERTO, StatusAprovacao.REJEITADA, pageable),
                conta -> ContaAbertaResponse.from(conta, hoje));
    }

    @Transactional(readOnly = true)
    public byte[] relatorioCsv(LocalDate dataDe, LocalDate dataAte, Membro membro) {
        autorizacao.exigirConsultaFinanceira(membro);
        validarPeriodo(dataDe, dataAte);
        Movimento anterior = movimentoAte(dataDe.minusDays(1));
        Movimento periodo = movimentoNoPeriodo(dataDe, dataAte);
        Pendencias pendencias = pendenciasAtuais(hoje());
        String csv = "Critério,Valor\r\n"
                + linha("Período inicial", dataDe)
                + linha("Período final", dataAte)
                + linha("Regime", "Caixa: liquidações e estornos pela data efetiva")
                + linha("Saldo inicial do período", MoneyValues.format(anterior.saldo()))
                + linha("Entradas", MoneyValues.format(periodo.recebimentos))
                + linha("Saídas", MoneyValues.format(periodo.pagamentos))
                + linha("Estornos de receitas", MoneyValues.format(periodo.estornosReceitas))
                + linha("Estornos de despesas", MoneyValues.format(periodo.estornosDespesas))
                + linha("Saldo final", MoneyValues.format(anterior.saldo().add(periodo.saldo())))
                + linha("Pendências a receber", MoneyValues.format(pendencias.receber))
                + linha("Pendências a pagar", MoneyValues.format(pendencias.pagar))
                + linha("Contas vencidas", pendencias.vencidas);
        return csv.getBytes(StandardCharsets.UTF_8);
    }

    private Movimento movimentoAte(LocalDate data) {
        return movimentoNoPeriodo(null, data);
    }

    private Movimento movimentoNoPeriodo(LocalDate dataDe, LocalDate dataAte) {
        Long atleticaId = resolver.atleticaIdAtual();
        Movimento resultado = new Movimento();
        for (LancamentoFinanceiro lancamento : lancamentos.findAllByAtletica_Id(atleticaId)) {
            if (lancamento.getStatusLiquidacao() != StatusLiquidacao.LIQUIDADO
                    || !noPeriodo(lancamento.getDataLiquidacao(), dataDe, dataAte)) {
                continue;
            }
            if (lancamento.getTipo() == TipoLancamentoFinanceiro.RECEITA) {
                resultado.recebimentos = resultado.recebimentos.add(lancamento.getValor());
            } else {
                resultado.pagamentos = resultado.pagamentos.add(lancamento.getValor());
            }
        }
        for (EstornoFinanceiro estorno : estornos.findAllByAtletica_Id(atleticaId)) {
            if (!noPeriodo(estorno.getDataEstorno(), dataDe, dataAte)) {
                continue;
            }
            if (estorno.getTipoLancamento() == TipoLancamentoFinanceiro.RECEITA) {
                resultado.estornosReceitas = resultado.estornosReceitas.add(estorno.getValorLancamento());
            } else {
                resultado.estornosDespesas = resultado.estornosDespesas.add(estorno.getValorLancamento());
            }
        }
        return resultado;
    }

    private Pendencias pendenciasAtuais(LocalDate referencia) {
        Pendencias resultado = new Pendencias();
        for (LancamentoFinanceiro lancamento : lancamentos.findAllByAtletica_Id(resolver.atleticaIdAtual())) {
            if (lancamento.getStatusLiquidacao() != StatusLiquidacao.ABERTO) {
                continue;
            }
            if (lancamento.getTipo() == TipoLancamentoFinanceiro.DESPESA
                    && lancamento.getStatusAprovacao() == StatusAprovacao.REJEITADA) {
                continue;
            }
            if (lancamento.getTipo() == TipoLancamentoFinanceiro.RECEITA) {
                resultado.receber = resultado.receber.add(lancamento.getValor());
            } else {
                resultado.pagar = resultado.pagar.add(lancamento.getValor());
            }
            if (lancamento.vencidoEm(referencia)) {
                resultado.vencidas++;
            }
        }
        return resultado;
    }

    private boolean noPeriodo(LocalDate data, LocalDate de, LocalDate ate) {
        return data != null && (de == null || !data.isBefore(de)) && (ate == null || !data.isAfter(ate));
    }

    private void validarPeriodo(LocalDate dataDe, LocalDate dataAte) {
        if (dataDe == null || dataAte == null) {
            throw new BusinessRuleException("As datas inicial e final do relatório são obrigatórias.");
        }
        if (dataAte.isBefore(dataDe)) {
            throw new BusinessRuleException("A data final não pode ser anterior à data inicial.");
        }
    }

    private LocalDate hoje() {
        return horaLocal();
    }

    private LocalDate horaLocal() {
        Long atleticaId = resolver.atleticaIdAtual();
        return atleticas.findByIdAndId(atleticaId, atleticaId)
                .map(atletica -> FusoHorarioAtletica.hoje(atletica.getFusoHorario()))
                .orElseGet(() -> FusoHorarioAtletica.hoje(null));
    }

    private String linha(String criterio, Object valor) {
        return celula(criterio) + ',' + celula(valor) + "\r\n";
    }

    private String celula(Object valor) {
        String texto = valor == null ? "" : valor.toString();
        if (texto.matches("^[\\s]*[=+\\-@].*")) {
            texto = "'" + texto;
        }
        return '"' + texto.replace("\"", "\"\"") + '"';
    }

    private static final class Movimento {
        private BigDecimal recebimentos = ZERO;
        private BigDecimal pagamentos = ZERO;
        private BigDecimal estornosReceitas = ZERO;
        private BigDecimal estornosDespesas = ZERO;

        private BigDecimal saldo() {
            return recebimentos.subtract(pagamentos).subtract(estornosReceitas).add(estornosDespesas);
        }
    }

    private static final class Pendencias {
        private BigDecimal receber = ZERO;
        private BigDecimal pagar = ZERO;
        private long vencidas;
    }
}
