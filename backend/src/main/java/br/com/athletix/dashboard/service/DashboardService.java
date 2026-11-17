package br.com.athletix.dashboard.service;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.atletica.repository.AtleticaRepository;
import br.com.athletix.auth.service.Capacidade;
import br.com.athletix.auth.service.CapacidadeService;
import br.com.athletix.cargo.entity.PerfilAcesso;
import br.com.athletix.dashboard.dto.DashboardResponses.Departamento;
import br.com.athletix.dashboard.dto.DashboardResponses.Evento;
import br.com.athletix.dashboard.dto.DashboardResponses.Financeiro;
import br.com.athletix.dashboard.dto.DashboardResponses.Movimento;
import br.com.athletix.dashboard.dto.DashboardResponses.Resumo;
import br.com.athletix.dashboard.dto.DashboardResponses.Tarefa;
import br.com.athletix.departamento.repository.DepartamentoRepository;
import br.com.athletix.evento.entity.StatusEvento;
import br.com.athletix.evento.repository.EventoRepository;
import br.com.athletix.financeiro.entity.StatusLiquidacao;
import br.com.athletix.financeiro.entity.TipoLancamentoFinanceiro;
import br.com.athletix.financeiro.repository.EstornoFinanceiroRepository;
import br.com.athletix.financeiro.repository.LancamentoFinanceiroRepository;
import br.com.athletix.financeiro.service.FinanceiroConsultaService;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.membro.entity.StatusMembro;
import br.com.athletix.membro.repository.MembroRepository;
import br.com.athletix.patrimonio.repository.ItemPatrimonioRepository;
import br.com.athletix.shared.money.MoneyValues;
import br.com.athletix.shared.tenancy.AtleticaResourceResolver;
import br.com.athletix.shared.time.FusoHorarioAtletica;
import br.com.athletix.tarefa.repository.TarefaRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

  private final LancamentoFinanceiroRepository lancamentos;
  private final EstornoFinanceiroRepository estornos;
  private final CapacidadeService capacidades;
  private final MembroRepository membros;
  private final EventoRepository eventos;
  private final TarefaRepository tarefas;
  private final ItemPatrimonioRepository itens;
  private final DepartamentoRepository departamentos;
  private final FinanceiroConsultaService financeiro;
  private final AtleticaResourceResolver resolver;
  private final AtleticaRepository atleticas;

  public DashboardService(
      MembroRepository membros,
      EventoRepository eventos,
      TarefaRepository tarefas,
      ItemPatrimonioRepository itens,
      DepartamentoRepository departamentos,
      FinanceiroConsultaService financeiro,
      AtleticaResourceResolver resolver,
      LancamentoFinanceiroRepository lancamentos,
      EstornoFinanceiroRepository estornos,
      CapacidadeService capacidades,
      AtleticaRepository atleticas) {
    this.lancamentos = lancamentos;
    this.estornos = estornos;
    this.capacidades = capacidades;
    this.membros = membros;
    this.eventos = eventos;
    this.tarefas = tarefas;
    this.itens = itens;
    this.departamentos = departamentos;
    this.financeiro = financeiro;
    this.resolver = resolver;
    this.atleticas = atleticas;
  }

  @Transactional(readOnly = true)
  public Resumo resumo(Membro membro) {
    Long atleticaId = resolver.atleticaIdAtual();
    long pendentes = membros.countByAtletica_IdAndStatus(atleticaId, StatusMembro.PENDENTE);
    long ativos = membros.countByAtletica_IdAndStatus(atleticaId, StatusMembro.ATIVO);
    long inativos = membros.countByAtletica_IdAndStatus(atleticaId, StatusMembro.INATIVO);
    var resumoFinanceiro = financeiro.resumo(null, membro);

    return new Resumo(
        pendentes + ativos + inativos,
        pendentes,
        ativos,
        inativos,
        eventos.countByAtletica_IdAndStatusIn(
            atleticaId, EnumSet.of(StatusEvento.INSCRICOES_ABERTAS, StatusEvento.EM_ANDAMENTO)),
        resumoFinanceiro.saldoDisponivel(),
        itens.countByAtletica_IdAndAtivoTrue(atleticaId),
        itens.sumQuantidadeTotalByAtleticaIdAndAtivoTrue(atleticaId));
  }

  @Transactional(readOnly = true)
  public Financeiro financeiro(Membro membro) {
    YearMonth fim = YearMonth.from(hojeDaAtletica());
    YearMonth inicio = fim.minusMonths(5);
    return new Financeiro(
        financeiro.resumo(null, membro), financeiro.evolucaoMensal(inicio, fim, membro));
  }

  @Transactional(readOnly = true)
  public List<Departamento> departamentos() {
    Long atleticaId = resolver.atleticaIdAtual();
    long totalAtivos = membros.countByAtletica_IdAndStatus(atleticaId, StatusMembro.ATIVO);
    Map<Long, Long> ativosPorDepartamento =
        membros
            .countByAtleticaIdAndStatusGroupedByDepartamento(atleticaId, StatusMembro.ATIVO)
            .stream()
            .collect(
                Collectors.toMap(
                    MembroRepository.ContagemPorDepartamento::getDepartamentoId,
                    MembroRepository.ContagemPorDepartamento::getQuantidade));

    return departamentos.findAllByAtletica_IdOrderByNomeAsc(atleticaId).stream()
        .map(
            departamento -> {
              long membrosAtivos = ativosPorDepartamento.getOrDefault(departamento.getId(), 0L);
              int participacao =
                  totalAtivos == 0 ? 0 : (int) Math.round(membrosAtivos * 100.0 / totalAtivos);
              return new Departamento(
                  departamento.getId(), departamento.getNome(), membrosAtivos, participacao);
            })
        .toList();
  }

  @Transactional(readOnly = true)
  public List<Evento> proximos() {
    Long atleticaId = resolver.atleticaIdAtual();
    return eventos
        .findAllByAtletica_IdAndStatusNotOrderByInicioAsc(
            atleticaId, StatusEvento.CONCLUIDO, PageRequest.of(0, 10))
        .stream()
        .map(
            evento ->
                new Evento(
                    evento.getId(),
                    evento.getNome(),
                    evento.getInicio(),
                    evento.getStatus().name()))
        .toList();
  }

  @Transactional(readOnly = true)
  public List<Tarefa> pendentes(Membro membro) {
    Long atleticaId = resolver.atleticaIdAtual();
    List<br.com.athletix.tarefa.entity.Tarefa> tarefasPendentes =
        membro.getPerfilAcesso() == PerfilAcesso.MEMBRO
            ? tarefas.findPendentesByAtleticaIdAndResponsavelMembroId(
                atleticaId, membro.getId(), PageRequest.of(0, 20))
            : tarefas.findPendentesByAtleticaId(atleticaId, PageRequest.of(0, 20));
    LocalDate hoje = hojeDaAtletica();

    return tarefasPendentes.stream()
        .map(
            tarefa ->
                new Tarefa(
                    tarefa.getId(),
                    tarefa.getTitulo(),
                    tarefa.getPrazo(),
                    tarefa.getStatus().name(),
                    tarefa.getPrazo() != null && tarefa.getPrazo().isBefore(hoje)))
        .toList();
  }

  @Transactional(readOnly = true)
  public List<Movimento> movimentacoes(Membro membro) {
    if (!capacidades
        .para(membro.getPerfilAcesso())
        .contains(Capacidade.CONSULTAR_RESUMOS_FINANCEIROS)) {
      return List.of();
    }

    Long atleticaId = resolver.atleticaIdAtual();
    var limite = PageRequest.of(0, 10);
    List<MovimentoOrdenado> movimentos = new ArrayList<>();

    for (var lancamento :
        lancamentos
            .findByAtletica_IdAndStatusLiquidacaoAndDataLiquidacaoIsNotNullOrderByDataLiquidacaoDescIdDesc(
                atleticaId, StatusLiquidacao.LIQUIDADO, limite)) {
      var valor =
          lancamento.getTipo() == TipoLancamentoFinanceiro.DESPESA
              ? lancamento.getValor().negate()
              : lancamento.getValor();
      movimentos.add(
          new MovimentoOrdenado(
              lancamento.getDataLiquidacao(),
              lancamento.getId(),
              new Movimento(
                  lancamento.getTipo().name(),
                  lancamento.getDataLiquidacao(),
                  MoneyValues.format(valor),
                  lancamento.getDescricao())));
    }

    for (var estorno : estornos.findByAtletica_IdOrderByDataEstornoDescIdDesc(atleticaId, limite)) {
      var valor =
          estorno.getTipoLancamento() == TipoLancamentoFinanceiro.RECEITA
              ? estorno.getValorLancamento().negate()
              : estorno.getValorLancamento();
      movimentos.add(
          new MovimentoOrdenado(
              estorno.getDataEstorno(),
              estorno.getId(),
              new Movimento(
                  "ESTORNO",
                  estorno.getDataEstorno(),
                  MoneyValues.format(valor),
                  "Estorno: " + estorno.getDescricaoLancamento())));
    }

    return movimentos.stream()
        .sorted(
            Comparator.comparing(MovimentoOrdenado::data)
                .reversed()
                .thenComparing(MovimentoOrdenado::id, Comparator.reverseOrder())
                .thenComparing(movimento -> movimento.movimento().tipo()))
        .limit(10)
        .map(MovimentoOrdenado::movimento)
        .toList();
  }

  private LocalDate hojeDaAtletica() {
    Atletica atletica = atleticas.findById(resolver.atleticaIdAtual()).orElseThrow();
    return FusoHorarioAtletica.hoje(atletica.getFusoHorario());
  }

  private record MovimentoOrdenado(LocalDate data, Long id, Movimento movimento) {}
}
