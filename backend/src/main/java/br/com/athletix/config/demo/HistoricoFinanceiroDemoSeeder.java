package br.com.athletix.config.demo;

import static br.com.athletix.config.demo.DemoData.*;

import br.com.athletix.financeiro.dto.LancamentoFinanceiroResponse;
import br.com.athletix.financeiro.entity.TipoLancamentoFinanceiro;
import java.time.LocalDate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
class HistoricoFinanceiroDemoSeeder {
  private final FinanceiroDemoSeeder financeiro;

  HistoricoFinanceiroDemoSeeder(FinanceiroDemoSeeder financeiro) {
    this.financeiro = financeiro;
  }

  void criar(CenarioDemo cenario) {
    for (int mesesAtras = 1; mesesAtras <= MESES_DE_HISTORICO; mesesAtras++) {
      LocalDate competencia =
          cenario.hoje().minusMonths(mesesAtras).withDayOfMonth(DIA_COMPETENCIA_HISTORICA);
      LancamentoDemo dados =
          new LancamentoDemo(
              TipoLancamentoFinanceiro.RECEITA,
              RECEITA_HISTORICA_SINTETICA + mesesAtras,
              VALOR_300_REAIS,
              0,
              0);
      LancamentoFinanceiroResponse receita =
          financeiro.criarLancamento(cenario, dados, null, competencia);
      financeiro.liquidar(cenario, receita, competencia);
    }
  }
}
