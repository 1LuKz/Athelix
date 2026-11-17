package br.com.athletix.financeiro.dto;

import br.com.athletix.financeiro.entity.ModoDataLancamento;
import br.com.athletix.financeiro.entity.SituacaoLancamento;
import br.com.athletix.financeiro.entity.StatusAprovacao;
import br.com.athletix.financeiro.entity.TipoLancamentoFinanceiro;
import java.time.LocalDate;

public record LancamentoFinanceiroFiltro(
    ModoDataLancamento modoData,
    LocalDate dataDe,
    LocalDate dataAte,
    Long categoriaId,
    TipoLancamentoFinanceiro tipo,
    SituacaoLancamento situacao,
    StatusAprovacao statusAprovacao,
    Long eventoId,
    Long gestaoId) {}
