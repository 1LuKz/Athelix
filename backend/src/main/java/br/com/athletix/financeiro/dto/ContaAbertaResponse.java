package br.com.athletix.financeiro.dto;

import br.com.athletix.financeiro.entity.LancamentoFinanceiro;
import br.com.athletix.financeiro.entity.StatusAprovacao;
import br.com.athletix.financeiro.entity.TipoLancamentoFinanceiro;
import br.com.athletix.shared.money.MoneyValues;
import java.time.LocalDate;

public record ContaAbertaResponse(
    Long id,
    TipoLancamentoFinanceiro tipo,
    String descricao,
    Long categoriaId,
    String categoriaNome,
    String valor,
    LocalDate dataVencimento,
    boolean atrasada,
    StatusAprovacao statusAprovacao,
    Long eventoId,
    Long gestaoOrigemId,
    Long version) {
  public static ContaAbertaResponse from(LancamentoFinanceiro lancamento, LocalDate hoje) {
    return new ContaAbertaResponse(
        lancamento.getId(),
        lancamento.getTipo(),
        lancamento.getDescricao(),
        lancamento.getCategoriaId(),
        lancamento.getCategoriaNome(),
        MoneyValues.format(lancamento.getValor()),
        lancamento.getDataVencimento(),
        lancamento.vencidoEm(hoje),
        lancamento.getStatusAprovacao(),
        lancamento.getEventoId(),
        lancamento.getGestaoOrigemId(),
        lancamento.getVersion());
  }
}
