package br.com.athletix.config.demo;

import static br.com.athletix.config.demo.DemoData.*;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.auditoria.service.ContextoAtividade;
import br.com.athletix.config.demo.EventosDemoSeeder.Eventos;
import br.com.athletix.financeiro.dto.EstornoFinanceiroCreateRequest;
import br.com.athletix.financeiro.dto.LancamentoAprovacaoRequest;
import br.com.athletix.financeiro.dto.LancamentoFinanceiroCreateRequest;
import br.com.athletix.financeiro.dto.LancamentoFinanceiroResponse;
import br.com.athletix.financeiro.dto.LancamentoLiquidacaoRequest;
import br.com.athletix.financeiro.entity.CategoriaFinanceira;
import br.com.athletix.financeiro.entity.DecisaoAprovacaoFinanceira;
import br.com.athletix.financeiro.entity.TipoAplicavelCategoria;
import br.com.athletix.financeiro.repository.CategoriaFinanceiraRepository;
import br.com.athletix.financeiro.service.LancamentoFinanceiroService;
import br.com.athletix.shared.normalization.NomeNormalizado;
import java.time.LocalDate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
class FinanceiroDemoSeeder {
  private final CategoriaFinanceiraRepository categorias;
  private final LancamentoFinanceiroService lancamentos;
  private final ContextoAtividade contextoAtividade;

  FinanceiroDemoSeeder(
      CategoriaFinanceiraRepository categorias,
      LancamentoFinanceiroService lancamentos,
      ContextoAtividade contextoAtividade) {
    this.categorias = categorias;
    this.lancamentos = lancamentos;
    this.contextoAtividade = contextoAtividade;
  }

  void criar(CenarioDemo cenario, Eventos eventos) {
    LancamentoFinanceiroResponse receita =
        criarLancamento(cenario, RECEITA_PATROCINIO, eventos.planejado().getId(), cenario.hoje());
    liquidar(cenario, receita, cenario.hoje().minusDays(LIQUIDACAO_PATROCINIO_DIAS));
    LancamentoFinanceiroResponse despesa =
        aprovar(
            cenario,
            criarLancamento(cenario, DESPESA_LOCACAO, eventos.planejado().getId(), cenario.hoje()),
            DecisaoAprovacaoFinanceira.APROVAR);
    liquidar(cenario, despesa, cenario.hoje().minusDays(LIQUIDACAO_LOCACAO_DIAS));
    criarLancamento(cenario, DESPESA_MATERIAL, eventos.inscricoes().getId(), cenario.hoje());
    aprovar(
        cenario,
        criarLancamento(cenario, DESPESA_NAO_APROVADA, null, cenario.hoje()),
        DecisaoAprovacaoFinanceira.REJEITAR);
    aprovar(
        cenario,
        criarLancamento(cenario, DESPESA_VENCIDA, null, cenario.hoje()),
        DecisaoAprovacaoFinanceira.APROVAR);
    aprovar(
        cenario,
        criarLancamento(cenario, DESPESA_FUTURA, null, cenario.hoje()),
        DecisaoAprovacaoFinanceira.APROVAR);
    LancamentoFinanceiroResponse estornada =
        liquidar(
            cenario,
            criarLancamento(cenario, RECEITA_ESTORNADA, null, cenario.hoje()),
            cenario.hoje());
    estornar(cenario, estornada);
  }

  LancamentoFinanceiroResponse criarLancamento(
      CenarioDemo cenario, LancamentoDemo dados, Long eventoId, LocalDate referencia) {
    Long categoriaId =
        categorias.findAllByAtletica_IdOrderByNomeAsc(cenario.atletica().getId()).stream()
            .filter(categoria -> categoria.getNome().equals(EVENTOS))
            .findFirst()
            .orElseThrow()
            .getId();
    return lancamentos.criar(
        new LancamentoFinanceiroCreateRequest(
            eventoId,
            dados.tipo(),
            dados.descricao(),
            categoriaId,
            dados.valor(),
            referencia.plusDays(dados.competencia()),
            referencia.plusDays(dados.vencimento())),
        cenario.administrador());
  }

  LancamentoFinanceiroResponse liquidar(
      CenarioDemo cenario, LancamentoFinanceiroResponse lancamento, LocalDate data) {
    try (var escopoAtividade = abrirAutoriaFinanceira(cenario)) {
      return lancamentos.liquidar(
          lancamento.id(),
          new LancamentoLiquidacaoRequest(data, lancamento.version()),
          cenario.tesoureiro());
    }
  }

  private LancamentoFinanceiroResponse aprovar(
      CenarioDemo cenario,
      LancamentoFinanceiroResponse lancamento,
      DecisaoAprovacaoFinanceira decisao) {
    try (var escopoAtividade = abrirAutoriaFinanceira(cenario)) {
      return lancamentos.decidirAprovacao(
          lancamento.id(),
          new LancamentoAprovacaoRequest(
              decisao,
              decisao == DecisaoAprovacaoFinanceira.REJEITAR
                  ? NAO_NECESSARIA
                  : APROVADA_PARA_DEMONSTRACAO,
              lancamento.version()),
          cenario.tesoureiro());
    }
  }

  private void estornar(CenarioDemo cenario, LancamentoFinanceiroResponse estornada) {
    try (var escopoAtividade = abrirAutoriaFinanceira(cenario)) {
      lancamentos.estornar(
          estornada.id(),
          new EstornoFinanceiroCreateRequest(cenario.hoje(), ESTORNO_DEMONSTRATIVO),
          cenario.tesoureiro());
    }
  }

  private ContextoAtividade.Escopo abrirAutoriaFinanceira(CenarioDemo cenario) {
    return contextoAtividade.abrir(
        cenario.atletica().getId(),
        cenario.tesoureiro().getUsuarioId(),
        cenario.tesoureiro().getId());
  }

  void criarCategorias(Atletica atletica) {
    criarCategoria(atletica, EVENTOS, TipoAplicavelCategoria.AMBOS);
    criarCategoria(atletica, ESPORTIVO, TipoAplicavelCategoria.DESPESA);
    criarCategoria(atletica, PATROCINIO, TipoAplicavelCategoria.RECEITA);
  }

  private void criarCategoria(Atletica atletica, String nome, TipoAplicavelCategoria tipo) {
    categorias.save(
        new CategoriaFinanceira(
            atletica, nome, NomeNormalizado.fromRequired(nome, CAMPO_NOME), tipo, true));
  }
}
