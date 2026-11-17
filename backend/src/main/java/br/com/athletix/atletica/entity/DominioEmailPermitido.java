package br.com.athletix.atletica.entity;

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

/** Domínio de e-mail aceito exclusivamente para uma atlética. */
@Entity
@Table(
    name = "dominios_email_permitidos",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_dominio_email_atletica_dominio",
            columnNames = {"atletica_id", "dominio"}))
public class DominioEmailPermitido implements AtleticaScoped {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "atletica_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_dominio_email_atletica"))
  private Atletica atletica;

  @Column(nullable = false, length = 253)
  private String dominio;

  protected DominioEmailPermitido() {}

  public DominioEmailPermitido(Atletica atletica, String dominio) {
    this.atletica = atletica;
    this.dominio = dominio;
  }

  public Long getId() {
    return id;
  }

  @Override
  public Long getAtleticaId() {
    return atletica.getId();
  }

  public String getDominio() {
    return dominio;
  }
}
