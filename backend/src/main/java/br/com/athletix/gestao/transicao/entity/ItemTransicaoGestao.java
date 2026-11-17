package br.com.athletix.gestao.transicao.entity;

import br.com.athletix.shared.persistence.AuditableEntity;
import jakarta.persistence.*;

@Entity
@Table(
    name = "itens_transicao_gestao",
    uniqueConstraints = @UniqueConstraint(columnNames = {"transicao_id", "membro_id"}))
public class ItemTransicaoGestao extends AuditableEntity {
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "transicao_id", nullable = false)
  private TransicaoGestao transicao;

  @Column(name = "membro_id", nullable = false)
  private Long membroId;

  @Column(name = "cargo_atual_id", nullable = false)
  private Long cargoAtualId;

  @Column(name = "departamento_atual_id", nullable = false)
  private Long departamentoAtualId;

  @Column(name = "cargo_destino_id", nullable = false)
  private Long cargoDestinoId;

  @Column(name = "departamento_destino_id", nullable = false)
  private Long departamentoDestinoId;

  @Column(name = "continua_ativo", nullable = false)
  private boolean continuaAtivo;

  @Column(name = "substitui_responsavel_membro_id")
  private Long substituiResponsavelMembroId;

  protected ItemTransicaoGestao() {}

  public ItemTransicaoGestao(
      TransicaoGestao t, Long m, Long ca, Long da, Long cd, Long dd, boolean c, Long s) {
    transicao = t;
    membroId = m;
    cargoAtualId = ca;
    departamentoAtualId = da;
    cargoDestinoId = cd;
    departamentoDestinoId = dd;
    continuaAtivo = c;
    substituiResponsavelMembroId = s;
  }

  public Long getMembroId() {
    return membroId;
  }

  public Long getCargoAtualId() {
    return cargoAtualId;
  }

  public Long getDepartamentoAtualId() {
    return departamentoAtualId;
  }

  public Long getCargoDestinoId() {
    return cargoDestinoId;
  }

  public Long getDepartamentoDestinoId() {
    return departamentoDestinoId;
  }

  public boolean isContinuaAtivo() {
    return continuaAtivo;
  }

  public Long getSubstituiResponsavelMembroId() {
    return substituiResponsavelMembroId;
  }

  public void atualizar(Long c, Long d, boolean a, Long s) {
    cargoDestinoId = c;
    departamentoDestinoId = d;
    continuaAtivo = a;
    substituiResponsavelMembroId = s;
  }
}
