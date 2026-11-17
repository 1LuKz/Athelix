package br.com.athletix.financeiro.dto;

import br.com.athletix.shared.money.MoneyValues;
import java.math.BigDecimal;
import java.time.LocalDate;

public record FinanceiroResumoResponse(
    LocalDate dataReferencia,
    String saldoInicial,
    String recebimentos,
    String pagamentos,
    String estornosReceitas,
    String estornosDespesas,
    String saldoDisponivel,
    String contasAReceber,
    String contasAPagar,
    long contasVencidas) {
  public static FinanceiroResumoResponse of(
      LocalDate dataReferencia,
      BigDecimal saldoInicial,
      BigDecimal recebimentos,
      BigDecimal pagamentos,
      BigDecimal estornosReceitas,
      BigDecimal estornosDespesas,
      BigDecimal saldoDisponivel,
      BigDecimal contasAReceber,
      BigDecimal contasAPagar,
      long contasVencidas) {
    return new FinanceiroResumoResponse(
        dataReferencia,
        MoneyValues.format(saldoInicial),
        MoneyValues.format(recebimentos),
        MoneyValues.format(pagamentos),
        MoneyValues.format(estornosReceitas),
        MoneyValues.format(estornosDespesas),
        MoneyValues.format(saldoDisponivel),
        MoneyValues.format(contasAReceber),
        MoneyValues.format(contasAPagar),
        contasVencidas);
  }
}
