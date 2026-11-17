package br.com.athletix.membro.entity;

import br.com.athletix.cargo.entity.Cargo;
import br.com.athletix.departamento.entity.Departamento;
import br.com.athletix.shared.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

/** Pedido de mudança que mantém o vínculo original até avaliação administrativa. */
@Entity
@Table(name = "solicitacoes_alteracao_vinculo")
public class SolicitacaoAlteracaoVinculo extends AuditableEntity {
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "membro_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_solicitacao_vinculo_membro"))
  private Membro membro;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "cargo_pretendido_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_solicitacao_vinculo_cargo"))
  private Cargo cargoPretendido;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "departamento_pretendido_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_solicitacao_vinculo_departamento"))
  private Departamento departamentoPretendido;

  @Column(name = "cargo_anterior_id", nullable = false)
  private Long cargoAnteriorId;

  @Column(name = "departamento_anterior_id", nullable = false)
  private Long departamentoAnteriorId;

  @Column(nullable = false, length = 500)
  private String motivo;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private StatusSolicitacaoAlteracaoVinculo status;

  @Column(name = "avaliada_em")
  private Instant avaliadaEm;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "avaliada_por_membro_id",
      foreignKey = @ForeignKey(name = "fk_solicitacao_vinculo_avaliador"))
  private Membro avaliadaPorMembro;

  @Column(name = "observacao_avaliacao", length = 500)
  private String observacaoAvaliacao;

  protected SolicitacaoAlteracaoVinculo() {}

  public SolicitacaoAlteracaoVinculo(
      Membro membro, Cargo cargoPretendido, Departamento departamentoPretendido, String motivo) {
    this.membro = membro;
    this.cargoPretendido = cargoPretendido;
    this.departamentoPretendido = departamentoPretendido;
    this.cargoAnteriorId = membro.getCargoId();
    this.departamentoAnteriorId = membro.getDepartamentoId();
    this.motivo = motivo;
    this.status = StatusSolicitacaoAlteracaoVinculo.PENDENTE;
  }

  public Long getMembroId() {
    return membro.getId();
  }

  public Long getCargoPretendidoId() {
    return cargoPretendido.getId();
  }

  public String getNomeCargoPretendido() {
    return cargoPretendido.getNome();
  }

  public Cargo getCargoPretendido() {
    return cargoPretendido;
  }

  public Long getDepartamentoPretendidoId() {
    return departamentoPretendido.getId();
  }

  public String getNomeDepartamentoPretendido() {
    return departamentoPretendido.getNome();
  }

  public Departamento getDepartamentoPretendido() {
    return departamentoPretendido;
  }

  public Long getCargoAnteriorId() {
    return cargoAnteriorId;
  }

  public Long getDepartamentoAnteriorId() {
    return departamentoAnteriorId;
  }

  public String getMotivo() {
    return motivo;
  }

  public StatusSolicitacaoAlteracaoVinculo getStatus() {
    return status;
  }

  public Instant getAvaliadaEm() {
    return avaliadaEm;
  }

  public Long getAvaliadaPorMembroId() {
    return avaliadaPorMembro == null ? null : avaliadaPorMembro.getId();
  }

  public String getObservacaoAvaliacao() {
    return observacaoAvaliacao;
  }

  public void aprovar(Membro avaliador, String observacao) {
    this.status = StatusSolicitacaoAlteracaoVinculo.APROVADA;
    this.avaliadaPorMembro = avaliador;
    this.avaliadaEm = Instant.now();
    this.observacaoAvaliacao = observacao;
  }

  public void rejeitar(Membro avaliador, String observacao) {
    this.status = StatusSolicitacaoAlteracaoVinculo.REJEITADA;
    this.avaliadaPorMembro = avaliador;
    this.avaliadaEm = Instant.now();
    this.observacaoAvaliacao = observacao;
  }
}
