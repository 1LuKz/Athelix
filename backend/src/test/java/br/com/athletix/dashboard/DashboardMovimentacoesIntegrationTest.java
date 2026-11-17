package br.com.athletix.dashboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.atletica.repository.AtleticaRepository;
import br.com.athletix.auth.service.CapacidadeService;
import br.com.athletix.cargo.entity.PerfilAcesso;
import br.com.athletix.dashboard.service.DashboardService;
import br.com.athletix.financeiro.entity.*;
import br.com.athletix.financeiro.repository.*;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.shared.tenancy.ContextoAtletica;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class DashboardMovimentacoesIntegrationTest {
  @Autowired DashboardService dashboard;
  @Autowired AtleticaRepository atleticas;
  @Autowired CategoriaFinanceiraRepository categorias;
  @Autowired LancamentoFinanceiroRepository lancamentos;
  @Autowired EstornoFinanceiroRepository estornos;
  @Autowired ContextoAtletica contexto;
  @SpyBean CapacidadeService capacidades;

  @Test
  void ultimasMovimentacoesRespeitamAtleticaDatasSinaisLimiteECapacidade() {
    List<Atletica> todas = atleticas.findAll();
    assertThat(todas).hasSizeGreaterThanOrEqualTo(2);
    Atletica atual = todas.get(0), outra = todas.get(1);
    CategoriaFinanceira categoria =
        categorias.save(
            new CategoriaFinanceira(
                atual, "Teste dashboard", "TESTE DASHBOARD", TipoAplicavelCategoria.AMBOS, true));
    CategoriaFinanceira categoriaOutra =
        categorias.save(
            new CategoriaFinanceira(
                outra, "Teste externo", "TESTE EXTERNO", TipoAplicavelCategoria.AMBOS, true));
    LocalDate base = LocalDate.of(2099, 1, 1);
    LancamentoFinanceiro receita = null, despesa = null;
    for (int i = 0; i < 12; i++) {
      TipoLancamentoFinanceiro tipo =
          i % 2 == 0 ? TipoLancamentoFinanceiro.RECEITA : TipoLancamentoFinanceiro.DESPESA;
      var lancamento =
          new LancamentoFinanceiro(
              atual,
              null,
              null,
              tipo,
              "Movimento " + i,
              categoria,
              new BigDecimal("12.30"),
              base,
              null,
              1L);
      lancamento.liquidar(base.plusDays(i), 1L);
      lancamento = lancamentos.save(lancamento);
      if (i == 10) receita = lancamento;
      if (i == 11) despesa = lancamento;
    }
    lancamentos.save(
        new LancamentoFinanceiro(
            atual,
            null,
            null,
            TipoLancamentoFinanceiro.RECEITA,
            "Não liquidado",
            categoria,
            BigDecimal.TEN,
            base,
            null,
            1L));
    var externo =
        new LancamentoFinanceiro(
            outra,
            null,
            null,
            TipoLancamentoFinanceiro.RECEITA,
            "Outra atlética",
            categoriaOutra,
            BigDecimal.TEN,
            base,
            null,
            1L);
    externo.liquidar(base.plusDays(100), 1L);
    lancamentos.save(externo);
    estornos.save(
        new EstornoFinanceiro(
            atual, receita, base.plusDays(13), "Motivo privado", 1L, Instant.now()));
    estornos.save(
        new EstornoFinanceiro(
            atual, despesa, base.plusDays(12), "Motivo privado", 1L, Instant.now()));
    Membro membro = mock(Membro.class);
    when(membro.getPerfilAcesso()).thenReturn(PerfilAcesso.ADMINISTRADOR);
    try (var ignored = contexto.abrir(atual.getId())) {
      var movimentos = dashboard.movimentacoes(membro);
      assertThat(movimentos).hasSize(10);
      assertThat(movimentos.get(0).tipo()).isEqualTo("ESTORNO");
      assertThat(movimentos.get(0).valor()).isEqualTo("-12.30");
      assertThat(movimentos.get(0).data()).isEqualTo(base.plusDays(13));
      assertThat(movimentos.get(1).valor()).isEqualTo("12.30");
      assertThat(movimentos.get(2).tipo()).isEqualTo("DESPESA");
      assertThat(movimentos.get(2).valor()).isEqualTo("-12.30");
      assertThat(movimentos.get(3).tipo()).isEqualTo("RECEITA");
      assertThat(movimentos.get(3).valor()).isEqualTo("12.30");
      assertThat(movimentos)
          .allSatisfy(
              m ->
                  assertThat(m.descricao())
                      .doesNotContain("Outra atlética", "Não liquidado", "Motivo privado"));
      assertThat(movimentos.stream().map(m -> (LocalDate) m.data()).toList())
          .isSortedAccordingTo(java.util.Comparator.reverseOrder());
      doReturn(List.of()).when(capacidades).para(PerfilAcesso.MEMBRO);
      when(membro.getPerfilAcesso()).thenReturn(PerfilAcesso.MEMBRO);
      assertThat(dashboard.movimentacoes(membro)).isEmpty();
    }
  }
}
