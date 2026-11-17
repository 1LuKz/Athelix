package br.com.athletix.financeiro.entity;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.shared.tenancy.AtleticaScoped;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.time.LocalDate;

/** Estorno integral e imutável de um lançamento financeiro já liquidado. */
@Entity
@Table(
    name = "estornos_financeiros",
    uniqueConstraints =
        @UniqueConstraint(name = "uk_estorno_financeiro_lancamento", columnNames = "lancamento_id"))
public class EstornoFinanceiro implements AtleticaScoped {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "atletica_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_estorno_financeiro_atletica"))
  private Atletica atletica;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "lancamento_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_estorno_financeiro_lancamento"))
  private LancamentoFinanceiro lancamento;

  @Column(name = "data_estorno", nullable = false)
  private LocalDate dataEstorno;

  @Column(nullable = false, length = 1000)
  private String motivo;

  @Column(name = "registrado_por_membro_id", nullable = false)
  private Long registradoPorMembroId;

  @Column(name = "registrado_em", nullable = false, updatable = false)
  private Instant registradoEm;

  protected EstornoFinanceiro() {}

  public EstornoFinanceiro(
      Atletica atletica,
      LancamentoFinanceiro lancamento,
      LocalDate dataEstorno,
      String motivo,
      Long registradoPorMembroId,
      Instant registradoEm) {
    this.atletica = atletica;
    this.lancamento = lancamento;
    this.dataEstorno = dataEstorno;
    this.motivo = motivo;
    this.registradoPorMembroId = registradoPorMembroId;
    this.registradoEm = registradoEm;
  }

  public Long getId() {
    return id;
  }

  @Override
  public Long getAtleticaId() {
    return atletica.getId();
  }

  public String getDescricaoLancamento() {
    return lancamento.getDescricao();
  }

  public Long getLancamentoId() {
    return lancamento.getId();
  }

  public TipoLancamentoFinanceiro getTipoLancamento() {
    return lancamento.getTipo();
  }

  public java.math.BigDecimal getValorLancamento() {
    return lancamento.getValor();
  }

  public LocalDate getDataEstorno() {
    return dataEstorno;
  }

  public String getMotivo() {
    return motivo;
  }

  public Long getRegistradoPorMembroId() {
    return registradoPorMembroId;
  }

  public Instant getRegistradoEm() {
    return registradoEm;
  }
}
