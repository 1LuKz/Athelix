package br.com.athletix.financeiro.entity;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.evento.entity.Evento;
import br.com.athletix.shared.persistence.AuditableEntity;
import br.com.athletix.shared.tenancy.AtleticaScoped;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "lancamentos_financeiros")
public class LancamentoFinanceiro extends AuditableEntity implements AtleticaScoped {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "atletica_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_lancamento_financeiro_atletica"))
  private Atletica atletica;

  @Column(name = "gestao_origem_id")
  private Long gestaoOrigemId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "evento_id",
      foreignKey = @ForeignKey(name = "fk_lancamento_financeiro_evento"))
  private Evento evento;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 7)
  private TipoLancamentoFinanceiro tipo;

  @Column(nullable = false, length = 250)
  private String descricao;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "categoria_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_lancamento_financeiro_categoria"))
  private CategoriaFinanceira categoria;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal valor;

  @Column(name = "data_competencia", nullable = false)
  private LocalDate dataCompetencia;

  @Column(name = "data_vencimento")
  private LocalDate dataVencimento;

  @Column(name = "data_liquidacao")
  private LocalDate dataLiquidacao;

  @Enumerated(EnumType.STRING)
  @Column(name = "status_liquidacao", nullable = false, length = 10)
  private StatusLiquidacao statusLiquidacao;

  @Enumerated(EnumType.STRING)
  @Column(name = "status_aprovacao", nullable = false, length = 16)
  private StatusAprovacao statusAprovacao;

  @Column(name = "criado_por_membro_id", nullable = false)
  private Long criadoPorMembroId;

  @Column(name = "aprovado_por_membro_id")
  private Long aprovadoPorMembroId;

  @Column(name = "aprovado_em")
  private Instant aprovadoEm;

  @Column(name = "motivo_decisao", length = 1000)
  private String motivoDecisao;

  @Column(name = "liquidado_por_membro_id")
  private Long liquidadoPorMembroId;

  @Column(name = "motivo_cancelamento", length = 1000)
  private String motivoCancelamento;

  protected LancamentoFinanceiro() {}

  public LancamentoFinanceiro(
      Atletica atletica,
      Long gestaoOrigemId,
      Evento evento,
      TipoLancamentoFinanceiro tipo,
      String descricao,
      CategoriaFinanceira categoria,
      BigDecimal valor,
      LocalDate dataCompetencia,
      LocalDate dataVencimento,
      Long criadoPorMembroId) {
    this.atletica = atletica;
    this.gestaoOrigemId = gestaoOrigemId;
    this.evento = evento;
    this.tipo = tipo;
    this.descricao = descricao;
    this.categoria = categoria;
    this.valor = valor;
    this.dataCompetencia = dataCompetencia;
    this.dataVencimento = dataVencimento;
    this.criadoPorMembroId = criadoPorMembroId;
    this.statusLiquidacao = StatusLiquidacao.ABERTO;
    this.statusAprovacao =
        tipo == TipoLancamentoFinanceiro.RECEITA
            ? StatusAprovacao.NAO_APLICAVEL
            : StatusAprovacao.PENDENTE;
  }

  @Override
  public Long getAtleticaId() {
    return atletica.getId();
  }

  public Long getGestaoOrigemId() {
    return gestaoOrigemId;
  }

  public Long getEventoId() {
    return evento == null ? null : evento.getId();
  }

  public TipoLancamentoFinanceiro getTipo() {
    return tipo;
  }

  public String getDescricao() {
    return descricao;
  }

  public Long getCategoriaId() {
    return categoria.getId();
  }

  public String getCategoriaNome() {
    return categoria.getNome();
  }

  public BigDecimal getValor() {
    return valor;
  }

  public LocalDate getDataCompetencia() {
    return dataCompetencia;
  }

  public LocalDate getDataVencimento() {
    return dataVencimento;
  }

  public LocalDate getDataLiquidacao() {
    return dataLiquidacao;
  }

  public StatusLiquidacao getStatusLiquidacao() {
    return statusLiquidacao;
  }

  public StatusAprovacao getStatusAprovacao() {
    return statusAprovacao;
  }

  public Long getCriadoPorMembroId() {
    return criadoPorMembroId;
  }

  public Long getAprovadoPorMembroId() {
    return aprovadoPorMembroId;
  }

  public Instant getAprovadoEm() {
    return aprovadoEm;
  }

  public String getMotivoDecisao() {
    return motivoDecisao;
  }

  public Long getLiquidadoPorMembroId() {
    return liquidadoPorMembroId;
  }

  public String getMotivoCancelamento() {
    return motivoCancelamento;
  }

  public void atualizar(
      String descricao,
      CategoriaFinanceira categoria,
      BigDecimal valor,
      Evento evento,
      LocalDate dataCompetencia,
      LocalDate dataVencimento) {
    this.descricao = descricao;
    this.categoria = categoria;
    this.valor = valor;
    this.evento = evento;
    this.dataCompetencia = dataCompetencia;
    this.dataVencimento = dataVencimento;
  }

  public void decidirAprovacao(
      StatusAprovacao decisao, String motivo, Long membroId, Instant ocorridaEm) {
    this.statusAprovacao = decisao;
    this.motivoDecisao = motivo;
    this.aprovadoPorMembroId = membroId;
    this.aprovadoEm = ocorridaEm;
  }

  public void liquidar(LocalDate data, Long membroId) {
    this.statusLiquidacao = StatusLiquidacao.LIQUIDADO;
    this.dataLiquidacao = data;
    this.liquidadoPorMembroId = membroId;
  }

  public void cancelar(String motivo) {
    this.statusLiquidacao = StatusLiquidacao.CANCELADO;
    this.motivoCancelamento = motivo;
  }

  public boolean vencidoEm(LocalDate hoje) {
    return statusLiquidacao == StatusLiquidacao.ABERTO
        && dataVencimento != null
        && dataVencimento.isBefore(hoje);
  }
}
