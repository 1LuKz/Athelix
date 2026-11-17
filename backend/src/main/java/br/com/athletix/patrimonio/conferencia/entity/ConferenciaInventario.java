package br.com.athletix.patrimonio.conferencia.entity;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.shared.persistence.AuditableEntity;
import br.com.athletix.shared.tenancy.AtleticaScoped;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "conferencias_inventario")
public class ConferenciaInventario extends AuditableEntity implements AtleticaScoped {
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "atletica_id", nullable = false)
  private Atletica atletica;

  @Column(name = "gestao_id")
  private Long gestaoId;

  @Column(nullable = false, length = 120)
  private String nome;

  @Column(name = "iniciada_em", nullable = false)
  private Instant iniciadaEm;

  @Column(name = "encerrada_em")
  private Instant encerradaEm;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private StatusConferenciaInventario status;

  @Column(name = "criada_por_membro_id", nullable = false)
  private Long criadaPorMembroId;

  protected ConferenciaInventario() {}

  public ConferenciaInventario(Atletica a, Long g, String n, Instant i, Long c) {
    atletica = a;
    gestaoId = g;
    nome = n;
    iniciadaEm = i;
    criadaPorMembroId = c;
    status = StatusConferenciaInventario.ABERTA;
  }

  @Override
  public Long getAtleticaId() {
    return atletica.getId();
  }

  public Long getGestaoId() {
    return gestaoId;
  }

  public String getNome() {
    return nome;
  }

  public Instant getIniciadaEm() {
    return iniciadaEm;
  }

  public Instant getEncerradaEm() {
    return encerradaEm;
  }

  public StatusConferenciaInventario getStatus() {
    return status;
  }

  public Long getCriadaPorMembroId() {
    return criadaPorMembroId;
  }

  public void encerrar() {
    status = StatusConferenciaInventario.ENCERRADA;
    encerradaEm = Instant.now();
  }
}
