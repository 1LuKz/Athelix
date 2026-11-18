package br.com.athletix.config.demo;

import static br.com.athletix.config.demo.DemoData.*;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.config.demo.DemoData.LancamentoDemo;
import br.com.athletix.config.demo.EventosDemoSeeder.Eventos;
import br.com.athletix.evento.entity.Evento;
import br.com.athletix.financeiro.entity.CategoriaFinanceira;
import br.com.athletix.financeiro.entity.LancamentoFinanceiro;
import br.com.athletix.financeiro.entity.StatusAprovacao;
import br.com.athletix.financeiro.entity.TipoAplicavelCategoria;
import br.com.athletix.financeiro.entity.TipoLancamentoFinanceiro;
import br.com.athletix.financeiro.repository.CategoriaFinanceiraRepository;
import br.com.athletix.financeiro.repository.LancamentoFinanceiroRepository;
import br.com.athletix.gestao.entity.Gestao;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.shared.normalization.NomeNormalizado;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
class FinanceiroDemoSeeder {
  private final CategoriaFinanceiraRepository categoriaFinanceiraRepository;
  private final LancamentoFinanceiroRepository lancamentoRepository;

  FinanceiroDemoSeeder(
      CategoriaFinanceiraRepository categoriaFinanceiraRepository,
      LancamentoFinanceiroRepository lancamentoRepository) {
    this.categoriaFinanceiraRepository = categoriaFinanceiraRepository;
    this.lancamentoRepository = lancamentoRepository;
  }

  void criar(
      Atletica engenharia,
      Gestao atual,
      Eventos eventosDemo,
      Membro administradorMembro,
      Membro financeiroMembro,
      Membro gestorMembro,
      LocalDate hoje,
      Instant agora) {
    CategoriaFinanceira eventos =
        categoriaFinanceiraRepository.findAllByAtletica_IdOrderByNomeAsc(engenharia.getId()).get(0);
    LancamentoFinanceiro receita =
        salvarLancamento(
            engenharia,
            atual,
            eventosDemo.planejado(),
            eventos,
            RECEITA_PATROCINIO,
            hoje,
            administradorMembro);
    receita.liquidar(hoje.minusDays(LIQUIDACAO_PATROCINIO_DIAS), financeiroMembro.getId());
    lancamentoRepository.save(receita);
    LancamentoFinanceiro paga =
        salvarLancamento(
            engenharia,
            atual,
            eventosDemo.planejado(),
            eventos,
            DESPESA_LOCACAO,
            hoje,
            gestorMembro);
    paga.decidirAprovacao(
        StatusAprovacao.APROVADA,
        APROVADA_PARA_DEMONSTRACAO,
        financeiroMembro.getId(),
        agora.minus(Duration.ofDays(APROVACAO_LOCACAO_DIAS)));
    paga.liquidar(hoje.minusDays(LIQUIDACAO_LOCACAO_DIAS), financeiroMembro.getId());
    lancamentoRepository.save(paga);
    salvarLancamento(
        engenharia, atual, eventosDemo.inscricoes(), eventos, DESPESA_MATERIAL, hoje, gestorMembro);
    LancamentoFinanceiro rejeitada =
        salvarLancamento(
            engenharia, atual, null, eventos, DESPESA_NAO_APROVADA, hoje, gestorMembro);
    rejeitada.decidirAprovacao(
        StatusAprovacao.REJEITADA, NAO_NECESSARIA, financeiroMembro.getId(), agora);
    lancamentoRepository.save(rejeitada);
    salvarLancamento(engenharia, atual, null, eventos, DESPESA_VENCIDA, hoje, gestorMembro);
    registrarHistorico(engenharia, atual, eventos, administradorMembro, financeiroMembro, hoje);
  }

  private void registrarHistorico(
      Atletica atletica,
      Gestao gestao,
      CategoriaFinanceira categoria,
      Membro administrador,
      Membro financeiro,
      LocalDate hoje) {
    for (int mesesAtras = 1; mesesAtras <= MESES_DE_HISTORICO; mesesAtras++) {
      LocalDate competencia =
          hoje.minusMonths(mesesAtras).withDayOfMonth(DIA_COMPETENCIA_HISTORICA);
      LancamentoDemo dados =
          new LancamentoDemo(
              TipoLancamentoFinanceiro.RECEITA,
              RECEITA_HISTORICA_SINTETICA + mesesAtras,
              VALOR_300_REAIS,
              0,
              0);
      LancamentoFinanceiro historico =
          salvarLancamento(atletica, gestao, null, categoria, dados, competencia, administrador);
      historico.liquidar(competencia, financeiro.getId());
      lancamentoRepository.save(historico);
    }
  }

  void criarCategorias(Atletica atletica) {
    criarCategoria(atletica, EVENTOS, TipoAplicavelCategoria.AMBOS);
    criarCategoria(atletica, ESPORTIVO, TipoAplicavelCategoria.DESPESA);
    criarCategoria(atletica, PATROCINIO, TipoAplicavelCategoria.RECEITA);
  }

  private void criarCategoria(Atletica atletica, String nome, TipoAplicavelCategoria tipo) {
    categoriaFinanceiraRepository.save(
        new CategoriaFinanceira(
            atletica, nome, NomeNormalizado.fromRequired(nome, CAMPO_NOME), tipo, true));
  }

  private LancamentoFinanceiro salvarLancamento(
      Atletica atletica,
      Gestao gestao,
      Evento evento,
      CategoriaFinanceira categoria,
      LancamentoDemo dados,
      LocalDate hoje,
      Membro criador) {
    return lancamentoRepository.save(
        new LancamentoFinanceiro(
            atletica,
            gestao.getId(),
            evento,
            dados.tipo(),
            dados.descricao(),
            categoria,
            new BigDecimal(dados.valor()),
            hoje.plusDays(dados.competencia()),
            hoje.plusDays(dados.vencimento()),
            criador.getId()));
  }
}
