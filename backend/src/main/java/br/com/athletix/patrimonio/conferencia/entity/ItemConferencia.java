package br.com.athletix.patrimonio.conferencia.entity;

import br.com.athletix.shared.persistence.AuditableEntity;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "itens_conferencia",
    uniqueConstraints = @UniqueConstraint(columnNames = {"conferencia_id", "item_id"}))
public class ItemConferencia extends AuditableEntity {
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "conferencia_id", nullable = false)
  private ConferenciaInventario conferencia;

  @Column(name = "item_id", nullable = false)
  private Long itemId;

  @Column(name = "quantidade_esperada", nullable = false)
  private int quantidadeEsperada;

  @Column(name = "quantidade_verificada")
  private Integer quantidadeVerificada;

  private Integer divergencia;

  @Column(length = 1000)
  private String observacao;

  @Column(name = "conferida_por_membro_id")
  private Long conferidaPorMembroId;

  @Column(name = "conferida_em")
  private Instant conferidaEm;

  protected ItemConferencia() {}

  public ItemConferencia(ConferenciaInventario c, Long i, int q) {
    conferencia = c;
    itemId = i;
    quantidadeEsperada = q;
  }

  public Long getItemId() {
    return itemId;
  }

  public int getQuantidadeEsperada() {
    return quantidadeEsperada;
  }

  public Integer getQuantidadeVerificada() {
    return quantidadeVerificada;
  }

  public Integer getDivergencia() {
    return divergencia;
  }

  public String getObservacao() {
    return observacao;
  }

  public Long getConferidaPorMembroId() {
    return conferidaPorMembroId;
  }

  public Instant getConferidaEm() {
    return conferidaEm;
  }

  public void conferir(int q, String o, Long m) {
    quantidadeVerificada = q;
    divergencia = q - quantidadeEsperada;
    observacao = o;
    conferidaPorMembroId = m;
    conferidaEm = Instant.now();
  }
}
