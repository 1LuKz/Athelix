package br.com.athletix.financeiro.dto;

import br.com.athletix.financeiro.entity.EstornoFinanceiro;
import br.com.athletix.financeiro.entity.TipoLancamentoFinanceiro;
import br.com.athletix.shared.money.MoneyValues;

import java.time.Instant;
import java.time.LocalDate;

public record EstornoFinanceiroResponse(
        Long id, Long atleticaId, Long lancamentoId, TipoLancamentoFinanceiro tipoLancamento,
        String valor, LocalDate dataEstorno, String motivo, Long registradoPorMembroId, Instant registradoEm
) {
    public static EstornoFinanceiroResponse from(EstornoFinanceiro estorno) {
        return new EstornoFinanceiroResponse(estorno.getId(), estorno.getAtleticaId(), estorno.getLancamentoId(),
                estorno.getTipoLancamento(), MoneyValues.format(estorno.getValorLancamento()),
                estorno.getDataEstorno(), estorno.getMotivo(), estorno.getRegistradoPorMembroId(),
                estorno.getRegistradoEm());
    }
}
