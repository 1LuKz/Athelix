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
import br.com.athletix.financeiro.entity.EstornoFinanceiro;
import br.com.athletix.financeiro.entity.LancamentoFinanceiro;
import br.com.athletix.financeiro.entity.StatusLiquidacao;
import br.com.athletix.financeiro.entity.TipoLancamentoFinanceiro;
import br.com.athletix.financeiro.repository.EstornoFinanceiroRepository;
import br.com.athletix.financeiro.repository.LancamentoFinanceiroRepository;
import br.com.athletix.financeiro.service.FinanceiroConsultaService;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.membro.entity.StatusMembro;
import br.com.athletix.membro.repository.MembroRepository;
import br.com.athletix.patrimonio.conferencia.repository.ConferenciaInventarioRepository;
import br.com.athletix.patrimonio.conferencia.repository.ItemConferenciaRepository;
import br.com.athletix.patrimonio.repository.ItemPatrimonioRepository;
import br.com.athletix.shared.money.MoneyValues;
import br.com.athletix.shared.tenancy.AtleticaResourceResolver;
import br.com.athletix.shared.time.FusoHorarioAtletica;
import br.com.athletix.tarefa.repository.TarefaRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
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
  private final ConferenciaInventarioRepository conferencias;
  private final ItemConferenciaRepository itensConferencia;

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
      AtleticaRepository atleticas,
      ConferenciaInventarioRepository conferencias,
      ItemConferenciaRepository itensConferencia) {
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
    this.conferencias = conferencias;
    this.itensConferencia = itensConferencia;
  }

  @Transactional(readOnly = true)
  public Resumo resumo(Membro membro) {
    Long atleticaId = resolver.atleticaIdAtual();
    long pendentes = membros.countByAtletica_IdAndStatus(atleticaId, StatusMembro.PENDENTE);
    long ativos = membros.countByAtletica_IdAndStatus(atleticaId, StatusMembro.ATIVO);
    long inativos = membros.countByAtletica_IdAndStatus(atleticaId, StatusMembro.INATIVO);
    var resumoFinanceiro = financeiro.resumo(null, membro);
    LocalDate hoje = hojeDaAtletica();
    IndicadorConferencia indicadorConferencia = indicadorConferencia(atleticaId);

    return new Resumo(
        pendentes + ativos + inativos,
        pendentes,
        ativos,
        inativos,
        eventos.countByAtletica_IdAndStatusIn(
            atleticaId, EnumSet.of(StatusEvento.INSCRICOES_ABERTAS, StatusEvento.EM_ANDAMENTO)),
        resumoFinanceiro.saldoDisponivel(),
        itens.countByAtletica_IdAndAtivoTrue(atleticaId),
        itens.sumQuantidadeTotalByAtleticaIdAndAtivoTrue(atleticaId),
        novosMembrosNoMes(atleticaId, hoje),
        eventosNaSemana(atleticaId, hoje),
        variacaoSaldo(membro, hoje, resumoFinanceiro.saldoDisponivel()),
        indicadorConferencia.percentual(),
        indicadorConferencia.nomeCampanha());
  }

  private long novosMembrosNoMes(Long atleticaId, LocalDate hoje) {
    ZoneId zona = zonaDaAtletica();
    Instant inicio = hoje.withDayOfMonth(1).atStartOfDay(zona).toInstant();
    Instant fim = hoje.plusMonths(1).withDayOfMonth(1).atStartOfDay(zona).toInstant();
    return membros.countByAtletica_IdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
        atleticaId, inicio, fim);
  }

  private long eventosNaSemana(Long atleticaId, LocalDate hoje) {
    ZoneId zona = zonaDaAtletica();
    LocalDate inicioDaSemana = hoje.minusDays(hoje.getDayOfWeek().getValue() - 1L);
    Instant inicio = inicioDaSemana.atStartOfDay(zona).toInstant();
    Instant fim = inicioDaSemana.plusDays(7).atStartOfDay(zona).toInstant();
    return eventos.countByAtletica_IdAndStatusInAndInicioGreaterThanEqualAndInicioLessThan(
        atleticaId,
        EnumSet.of(
            StatusEvento.PLANEJADO, StatusEvento.INSCRICOES_ABERTAS, StatusEvento.EM_ANDAMENTO),
        inicio,
        fim);
  }

  private String variacaoSaldo(Membro membro, LocalDate hoje, String saldoAtual) {
    if (!capacidades
        .para(membro.getPerfilAcesso())
        .contains(Capacidade.CONSULTAR_RESUMOS_FINANCEIROS)) {
      return null;
    }
    BigDecimal saldoAnterior =
        new BigDecimal(
            financeiro.resumo(hoje.withDayOfMonth(1).minusDays(1), membro).saldoDisponivel());
    if (saldoAnterior.signum() == 0) return null;
    return new BigDecimal(saldoAtual)
        .subtract(saldoAnterior)
        .multiply(BigDecimal.valueOf(100))
        .divide(saldoAnterior.abs(), 1, RoundingMode.HALF_UP)
        .toPlainString();
  }

  private IndicadorConferencia indicadorConferencia(Long atleticaId) {
    return conferencias
        .findAllByAtletica_IdOrderByIniciadaEmDesc(atleticaId, PageRequest.of(0, 1))
        .stream()
        .findFirst()
        .map(
            conferencia -> {
              var progresso = itensConferencia.calcularProgressoDashboard(conferencia.getId());
              long esperado = progresso.getUnidadesEsperadas();
              int percentual =
                  esperado == 0
                      ? 0
                      : (int)
                          Math.round(progresso.getUnidadesVerificadasValidas() * 100.0 / esperado);
              return new IndicadorConferencia(percentual, conferencia.getNome());
            })
        .orElse(new IndicadorConferencia(null, null));
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
    var resumoPorDepartamento = departamentos.findResumoDashboardByAtleticaId(atleticaId);
    long totalAtivosComDepartamento =
        resumoPorDepartamento.stream()
            .mapToLong(DepartamentoRepository.ResumoDashboard::getMembrosAtivos)
            .sum();

    return resumoPorDepartamento.stream()
        .map(
            departamento -> {
              long membrosAtivos = departamento.getMembrosAtivos();
              int participacao =
                  totalAtivosComDepartamento == 0
                      ? 0
                      : (int) Math.round(membrosAtivos * 100.0 / totalAtivosComDepartamento);
              return new Departamento(
                  departamento.getId(), departamento.getNome(), membrosAtivos, participacao);
            })
        .toList();
  }

  @Transactional(readOnly = true)
  public List<Evento> proximos() {
    Long atleticaId = resolver.atleticaIdAtual();
    return eventos
        .findProximosParaDashboard(atleticaId, StatusEvento.CONCLUIDO, PageRequest.of(0, 10))
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
    List<TarefaRepository.TarefaPendenteDashboard> tarefasPendentes =
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
    adicionarLancamentos(movimentos, atleticaId, limite);
    adicionarEstornos(movimentos, atleticaId, limite);
    return ordenarMovimentos(movimentos);
  }

  private void adicionarLancamentos(
      List<MovimentoOrdenado> movimentos, Long atleticaId, PageRequest limite) {
    for (var lancamento :
        lancamentos
            .findByAtletica_IdAndStatusLiquidacaoAndDataLiquidacaoIsNotNullOrderByDataLiquidacaoDescIdDesc(
                atleticaId, StatusLiquidacao.LIQUIDADO, limite)) {
      movimentos.add(
          new MovimentoOrdenado(
              lancamento.getDataLiquidacao(),
              lancamento.getId(),
              new Movimento(
                  lancamento.getTipo().name(),
                  lancamento.getDataLiquidacao(),
                  MoneyValues.format(valorLancamento(lancamento)),
                  lancamento.getDescricao())));
    }
  }

  private BigDecimal valorLancamento(LancamentoFinanceiro lancamento) {
    return lancamento.getTipo() == TipoLancamentoFinanceiro.DESPESA
        ? lancamento.getValor().negate()
        : lancamento.getValor();
  }

  private void adicionarEstornos(
      List<MovimentoOrdenado> movimentos, Long atleticaId, PageRequest limite) {
    for (var estorno : estornos.findByAtletica_IdOrderByDataEstornoDescIdDesc(atleticaId, limite)) {
      movimentos.add(
          new MovimentoOrdenado(
              estorno.getDataEstorno(),
              estorno.getId(),
              new Movimento(
                  "ESTORNO",
                  estorno.getDataEstorno(),
                  MoneyValues.format(valorEstorno(estorno)),
                  "Estorno: " + estorno.getDescricaoLancamento())));
    }
  }

  private BigDecimal valorEstorno(EstornoFinanceiro estorno) {
    return estorno.getTipoLancamento() == TipoLancamentoFinanceiro.RECEITA
        ? estorno.getValorLancamento().negate()
        : estorno.getValorLancamento();
  }

  private List<Movimento> ordenarMovimentos(List<MovimentoOrdenado> movimentos) {
    return movimentos.stream()
        .sorted(
            Comparator.comparing(MovimentoOrdenado::data)
                .reversed()
                .thenComparing(MovimentoOrdenado::movimentoId, Comparator.reverseOrder())
                .thenComparing(movimento -> movimento.movimento().tipo()))
        .limit(10)
        .map(MovimentoOrdenado::movimento)
        .toList();
  }

  private LocalDate hojeDaAtletica() {
    Atletica atletica = atleticas.findById(resolver.atleticaIdAtual()).orElseThrow();
    return FusoHorarioAtletica.hoje(atletica.getFusoHorario());
  }

  private ZoneId zonaDaAtletica() {
    Atletica atletica = atleticas.findById(resolver.atleticaIdAtual()).orElseThrow();
    return FusoHorarioAtletica.zona(atletica.getFusoHorario());
  }

  private record MovimentoOrdenado(LocalDate data, Long movimentoId, Movimento movimento) {}

  private record IndicadorConferencia(Integer percentual, String nomeCampanha) {}
}
