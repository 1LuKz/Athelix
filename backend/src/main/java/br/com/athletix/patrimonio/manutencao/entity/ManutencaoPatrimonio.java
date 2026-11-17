package br.com.athletix.patrimonio.manutencao.entity;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.patrimonio.entity.ItemPatrimonio;
import br.com.athletix.shared.persistence.AuditableEntity;
import br.com.athletix.shared.tenancy.AtleticaScoped;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "manutencoes_patrimonio")
public class ManutencaoPatrimonio extends AuditableEntity implements AtleticaScoped {
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "atletica_id", nullable = false)
  private Atletica atletica;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "item_id", nullable = false)
  private ItemPatrimonio item;

  @Column(nullable = false)
  private int quantidade;

  @Column(nullable = false, length = 1000)
  private String motivo;

  @Column(name = "iniciada_em", nullable = false)
  private Instant iniciadaEm;

  @Column(name = "previsao_conclusao_em")
  private Instant previsaoConclusaoEm;

  @Column(name = "concluida_em")
  private Instant concluidaEm;

  @Column(length = 1000)
  private String resultado;

  @Column(name = "registrado_por_membro_id", nullable = false)
  private Long registradoPorMembroId;

  protected ManutencaoPatrimonio() {}

  public ManutencaoPatrimonio(
      Atletica atletica,
      ItemPatrimonio item,
      int quantidade,
      String motivo,
      Instant iniciadaEm,
      Instant previsaoConclusaoEm,
      Long registradoPorMembroId) {
    this.atletica = atletica;
    this.item = item;
    this.quantidade = quantidade;
    this.motivo = motivo;
    this.iniciadaEm = iniciadaEm;
    this.previsaoConclusaoEm = previsaoConclusaoEm;
    this.registradoPorMembroId = registradoPorMembroId;
  }

  @Override
  public Long getAtleticaId() {
    return atletica.getId();
  }

  public Long getItemId() {
    return item.getId();
  }

  public int getQuantidade() {
    return quantidade;
  }

  public String getMotivo() {
    return motivo;
  }

  public Instant getIniciadaEm() {
    return iniciadaEm;
  }

  public Instant getPrevisaoConclusaoEm() {
    return previsaoConclusaoEm;
  }

  public Instant getConcluidaEm() {
    return concluidaEm;
  }

  public String getResultado() {
    return resultado;
  }

  public Long getRegistradoPorMembroId() {
    return registradoPorMembroId;
  }

  public boolean isAberta() {
    return concluidaEm == null;
  }

  public void concluir(String resultado, Instant concluidaEm) {
    this.resultado = resultado;
    this.concluidaEm = concluidaEm;
  }
}
