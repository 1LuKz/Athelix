package br.com.athletix.departamento.entity;

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
    name = "departamentos",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_departamento_atletica_nome_normalizado",
            columnNames = {"atletica_id", "nome_normalizado"}))
public class Departamento extends AuditableEntity implements AtleticaScoped {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "atletica_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_departamento_atletica"))
  private Atletica atletica;

  @Column(nullable = false, length = 100)
  private String nome;

  @Column(name = "nome_normalizado", nullable = false, length = 100)
  private String nomeNormalizado;

  @Column(length = 1000)
  private String descricao;

  /** Será associada à entidade Membro na B05. */
  @Column(name = "responsavel_membro_id")
  private Long responsavelMembroId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private SituacaoDepartamento situacao;

  protected Departamento() {}

  public Departamento(Atletica atletica, String nome, String nomeNormalizado, String descricao) {
    this.atletica = atletica;
    this.nome = nome;
    this.nomeNormalizado = nomeNormalizado;
    this.descricao = descricao;
    this.situacao = SituacaoDepartamento.EM_CONFIGURACAO;
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

  public Long getResponsavelMembroId() {
    return responsavelMembroId;
  }

  public SituacaoDepartamento getSituacao() {
    return situacao;
  }

  public void atualizarDadosBasicos(String nome, String nomeNormalizado, String descricao) {
    this.nome = nome;
    this.nomeNormalizado = nomeNormalizado;
    this.descricao = descricao;
  }

  public void definirResponsavel(Long responsavelMembroId) {
    this.responsavelMembroId = responsavelMembroId;
  }

  public void ativar() {
    this.situacao = SituacaoDepartamento.ATIVO;
  }

  public void inativar() {
    this.situacao = SituacaoDepartamento.INATIVO;
  }
}
