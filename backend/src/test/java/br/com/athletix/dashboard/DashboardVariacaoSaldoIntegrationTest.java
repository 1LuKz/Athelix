package br.com.athletix.dashboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.atletica.repository.AtleticaRepository;
import br.com.athletix.auth.service.CapacidadeService;
import br.com.athletix.cargo.entity.PerfilAcesso;
import br.com.athletix.dashboard.service.DashboardService;
import br.com.athletix.evento.repository.EventoRepository;
import br.com.athletix.financeiro.entity.CategoriaFinanceira;
import br.com.athletix.financeiro.entity.LancamentoFinanceiro;
import br.com.athletix.financeiro.entity.TipoAplicavelCategoria;
import br.com.athletix.financeiro.entity.TipoLancamentoFinanceiro;
import br.com.athletix.financeiro.repository.CategoriaFinanceiraRepository;
import br.com.athletix.financeiro.repository.LancamentoFinanceiroRepository;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.shared.tenancy.ContextoAtletica;
import br.com.athletix.shared.time.FusoHorarioAtletica;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class DashboardVariacaoSaldoIntegrationTest {

  @Autowired private DashboardService dashboard;
  @Autowired private AtleticaRepository atleticas;
  @Autowired private CategoriaFinanceiraRepository categorias;
  @Autowired private LancamentoFinanceiroRepository lancamentos;
  @Autowired private ContextoAtletica contexto;
  @SpyBean private CapacidadeService capacidades;
  @SpyBean private EventoRepository eventos;

  @BeforeEach
  void impedirConsultaLateralDeEventos() {
    doReturn(0L)
        .when(eventos)
        .countByAtletica_IdAndStatusInAndInicioGreaterThanEqualAndInicioLessThan(
            anyLong(), any(), any(), any());
  }

  @Test
  void calculaAltaComSaldoAnteriorPositivo() {
    Atletica atletica = atleticas.findAll().get(0);
    LocalDate hoje = FusoHorarioAtletica.hoje(atletica.getFusoHorario());
    CategoriaFinanceira categoria = criarCategoria(atletica);
    salvarLancamentoLiquidado(
        atletica,
        categoria,
        TipoLancamentoFinanceiro.RECEITA,
        "100.00",
        ultimoDiaDoMesAnterior(hoje));
    salvarLancamentoLiquidado(atletica, categoria, TipoLancamentoFinanceiro.RECEITA, "20.00", hoje);

    try (var ignored = contexto.abrir(atletica.getId())) {
      assertThat(dashboard.resumo(membroAdministrador()).variacaoSaldoPercentual())
          .isEqualTo("20.0");
    }
  }

  @Test
  void calculaQuedaComSaldoAnteriorPositivo() {
    Atletica atletica = atleticas.findAll().get(0);
    LocalDate hoje = FusoHorarioAtletica.hoje(atletica.getFusoHorario());
    CategoriaFinanceira categoria = criarCategoria(atletica);
    salvarLancamentoLiquidado(
        atletica,
        categoria,
        TipoLancamentoFinanceiro.RECEITA,
        "100.00",
        ultimoDiaDoMesAnterior(hoje));
    salvarLancamentoLiquidado(atletica, categoria, TipoLancamentoFinanceiro.DESPESA, "20.00", hoje);

    try (var ignored = contexto.abrir(atletica.getId())) {
      assertThat(dashboard.resumo(membroAdministrador()).variacaoSaldoPercentual())
          .isEqualTo("-20.0");
    }
  }

  @Test
  void calculaAltaComSaldoAnteriorNegativo() {
    Atletica atletica = atleticas.findAll().get(0);
    LocalDate hoje = FusoHorarioAtletica.hoje(atletica.getFusoHorario());
    CategoriaFinanceira categoria = criarCategoria(atletica);
    salvarLancamentoLiquidado(
        atletica,
        categoria,
        TipoLancamentoFinanceiro.DESPESA,
        "100.00",
        ultimoDiaDoMesAnterior(hoje));
    salvarLancamentoLiquidado(atletica, categoria, TipoLancamentoFinanceiro.RECEITA, "20.00", hoje);

    try (var ignored = contexto.abrir(atletica.getId())) {
      assertThat(dashboard.resumo(membroAdministrador()).variacaoSaldoPercentual())
          .isEqualTo("20.0");
    }
  }

  @Test
  void naoCalculaQuandoSaldoAnteriorEZero() {
    Atletica atletica = atleticas.findAll().get(0);
    LocalDate hoje = FusoHorarioAtletica.hoje(atletica.getFusoHorario());
    CategoriaFinanceira categoria = criarCategoria(atletica);
    salvarLancamentoLiquidado(
        atletica, categoria, TipoLancamentoFinanceiro.RECEITA, "100.00", hoje);

    try (var ignored = contexto.abrir(atletica.getId())) {
      assertThat(dashboard.resumo(membroAdministrador()).variacaoSaldoPercentual()).isNull();
    }
  }

  @Test
  void naoCalculaSemCapacidadeDeConsultarResumosFinanceiros() {
    Atletica atletica = atleticas.findAll().get(0);
    Membro membro = membroAdministrador();
    doReturn(List.of()).when(capacidades).para(PerfilAcesso.ADMINISTRADOR);

    try (var ignored = contexto.abrir(atletica.getId())) {
      assertThat(dashboard.resumo(membro).variacaoSaldoPercentual()).isNull();
    }
  }

  private void salvarLancamentoLiquidado(
      Atletica atletica,
      CategoriaFinanceira categoria,
      TipoLancamentoFinanceiro tipo,
      String valor,
      LocalDate dataLiquidacao) {
    LancamentoFinanceiro lancamento =
        new LancamentoFinanceiro(
            atletica,
            null,
            null,
            tipo,
            "Lançamento do dashboard",
            categoria,
            new BigDecimal(valor),
            dataLiquidacao,
            null,
            1L);
    lancamento.liquidar(dataLiquidacao, 1L);
    lancamentos.save(lancamento);
  }

  private CategoriaFinanceira criarCategoria(Atletica atletica) {
    return categorias.save(
        new CategoriaFinanceira(
            atletica,
            "Categoria dashboard",
            "CATEGORIA DASHBOARD",
            TipoAplicavelCategoria.AMBOS,
            true));
  }

  private LocalDate ultimoDiaDoMesAnterior(LocalDate hoje) {
    return hoje.withDayOfMonth(1).minusDays(1);
  }

  private Membro membroAdministrador() {
    Membro membro = mock(Membro.class);
    when(membro.getPerfilAcesso()).thenReturn(PerfilAcesso.ADMINISTRADOR);
    return membro;
  }
}
