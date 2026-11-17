package br.com.athletix.patrimonio.emprestimo.entity;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.patrimonio.entity.ItemPatrimonio;
import br.com.athletix.shared.persistence.AuditableEntity;
import br.com.athletix.shared.tenancy.AtleticaScoped;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "emprestimos_patrimonio")
public class EmprestimoPatrimonio extends AuditableEntity implements AtleticaScoped {
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "atletica_id", nullable = false)
  private Atletica atletica;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "item_id", nullable = false)
  private ItemPatrimonio item;

  @Column(name = "membro_responsavel_id", nullable = false)
  private Long membroResponsavelId;

  @Column(nullable = false)
  private int quantidade;

  @Column(name = "retirado_em", nullable = false)
  private Instant retiradoEm;

  @Column(name = "devolucao_prevista_em")
  private Instant devolucaoPrevistaEm;

  @Column(length = 1000)
  private String observacao;

  @Column(name = "registrado_por_membro_id", nullable = false)
  private Long registradoPorMembroId;

  protected EmprestimoPatrimonio() {}

  public EmprestimoPatrimonio(
      Atletica atletica,
      ItemPatrimonio item,
      Long membroResponsavelId,
      int quantidade,
      Instant retiradoEm,
      Instant devolucaoPrevistaEm,
      String observacao,
      Long registradoPorMembroId) {
    this.atletica = atletica;
    this.item = item;
    this.membroResponsavelId = membroResponsavelId;
    this.quantidade = quantidade;
    this.retiradoEm = retiradoEm;
    this.devolucaoPrevistaEm = devolucaoPrevistaEm;
    this.observacao = observacao;
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

  public Long getMembroResponsavelId() {
    return membroResponsavelId;
  }

  public Instant getRetiradoEm() {
    return retiradoEm;
  }

  public Instant getDevolucaoPrevistaEm() {
    return devolucaoPrevistaEm;
  }

  public String getObservacao() {
    return observacao;
  }

  public Long getRegistradoPorMembroId() {
    return registradoPorMembroId;
  }
}
