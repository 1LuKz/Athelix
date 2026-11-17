package br.com.athletix.cargo.entity;

import br.com.athletix.atletica.entity.Atletica;
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

@Entity
@Table(
    name = "cargos",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_cargo_atletica_nome_normalizado",
            columnNames = {"atletica_id", "nome_normalizado"}))
public class Cargo extends AuditableEntity implements AtleticaScoped {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "atletica_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_cargo_atletica"))
  private Atletica atletica;

  @Column(nullable = false, length = 100)
  private String nome;

  @Column(name = "nome_normalizado", nullable = false, length = 100)
  private String nomeNormalizado;

  @Column(length = 500)
  private String descricao;

  @Enumerated(EnumType.STRING)
  @Column(name = "perfil_acesso", nullable = false, length = 20)
  private PerfilAcesso perfilAcesso;

  @Column(nullable = false)
  private boolean ativo;

  protected Cargo() {}

  public Cargo(
      Atletica atletica,
      String nome,
      String nomeNormalizado,
      String descricao,
      PerfilAcesso perfilAcesso,
      boolean ativo) {
    this.atletica = atletica;
    this.nome = nome;
    this.nomeNormalizado = nomeNormalizado;
    this.descricao = descricao;
    this.perfilAcesso = perfilAcesso;
    this.ativo = ativo;
  }

  @Override
  public Long getAtleticaId() {
    return atletica.getId();
  }

  public String getNome() {
    return nome;
  }

  public String getDescricao() {
    return descricao;
  }

  public PerfilAcesso getPerfilAcesso() {
    return perfilAcesso;
  }

  public boolean isAtivo() {
    return ativo;
  }

  public void atualizar(
      String nome,
      String nomeNormalizado,
      String descricao,
      PerfilAcesso perfilAcesso,
      boolean ativo) {
    this.nome = nome;
    this.nomeNormalizado = nomeNormalizado;
    this.descricao = descricao;
    this.perfilAcesso = perfilAcesso;
    this.ativo = ativo;
  }

  public void inativar() {
    this.ativo = false;
  }
}
