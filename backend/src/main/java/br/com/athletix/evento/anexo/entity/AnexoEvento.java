package br.com.athletix.evento.anexo.entity;

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
import jakarta.persistence.UniqueConstraint;

/** Vínculo auditável entre um evento e um arquivo protegido. */
@Entity
@Table(
    name = "anexos_evento",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_anexo_evento_arquivo",
            columnNames = {"evento_id", "arquivo_id"}))
public class AnexoEvento extends AuditableEntity implements AtleticaScoped {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "atletica_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_anexo_evento_atletica"))
  private Atletica atletica;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "evento_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_anexo_evento_evento"))
  private Evento evento;

  @Column(name = "arquivo_id", nullable = false)
  private Long arquivoId;

  @Column(nullable = false, length = 160)
  private String titulo;

  @Column(length = 1000)
  private String descricao;

  @Enumerated(EnumType.STRING)
  @Column(name = "tipo_material", nullable = false, length = 16)
  private TipoMaterialEvento tipoMaterial;

  protected AnexoEvento() {}

  public AnexoEvento(
      Atletica atletica,
      Evento evento,
      Long arquivoId,
      String titulo,
      String descricao,
      TipoMaterialEvento tipoMaterial) {
    this.atletica = atletica;
    this.evento = evento;
    this.arquivoId = arquivoId;
    this.titulo = titulo;
    this.descricao = descricao;
    this.tipoMaterial = tipoMaterial;
  }

  @Override
  public Long getAtleticaId() {
    return atletica.getId();
  }

  public Long getEventoId() {
    return evento.getId();
  }

  public Long getArquivoId() {
    return arquivoId;
  }

  public String getTitulo() {
    return titulo;
  }

  public String getDescricao() {
    return descricao;
  }

  public TipoMaterialEvento getTipoMaterial() {
    return tipoMaterial;
  }
}
