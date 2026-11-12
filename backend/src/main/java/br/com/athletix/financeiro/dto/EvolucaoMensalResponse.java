package br.com.athletix.financeiro.dto;

import br.com.athletix.shared.money.MoneyValues;

import java.math.BigDecimal;
import java.time.YearMonth;

public record EvolucaoMensalResponse(
        YearMonth mes, String receitas, String despesas, String estornosReceitas,
        String estornosDespesas, String saldo
) {
    public static EvolucaoMensalResponse of(YearMonth mes, BigDecimal receitas, BigDecimal despesas,
                                             BigDecimal estornosReceitas, BigDecimal estornosDespesas) {
        return new EvolucaoMensalResponse(mes, MoneyValues.format(receitas), MoneyValues.format(despesas),
                MoneyValues.format(estornosReceitas), MoneyValues.format(estornosDespesas),
                MoneyValues.format(receitas.subtract(despesas)));
    }
}
