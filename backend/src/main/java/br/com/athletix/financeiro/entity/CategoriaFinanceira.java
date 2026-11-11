package br.com.athletix.financeiro.entity;

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
@Table(name = "categorias_financeiras", uniqueConstraints = @UniqueConstraint(
        name = "uk_categoria_financeira_atletica_nome_normalizado",
        columnNames = {"atletica_id", "nome_normalizado"}
))
public class CategoriaFinanceira extends AuditableEntity implements AtleticaScoped {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "atletica_id", nullable = false, foreignKey = @ForeignKey(name = "fk_categoria_financeira_atletica"))
    private Atletica atletica;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(name = "nome_normalizado", nullable = false, length = 100)
    private String nomeNormalizado;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_aplicavel", nullable = false, length = 8)
    private TipoAplicavelCategoria tipoAplicavel;

    @Column(nullable = false)
    private boolean ativa;

    protected CategoriaFinanceira() {
    }

    public CategoriaFinanceira(Atletica atletica, String nome, String nomeNormalizado,
                               TipoAplicavelCategoria tipoAplicavel, boolean ativa) {
        this.atletica = atletica;
        this.nome = nome;
        this.nomeNormalizado = nomeNormalizado;
        this.tipoAplicavel = tipoAplicavel;
        this.ativa = ativa;
    }

    @Override
    public Long getAtleticaId() {
        return atletica.getId();
    }

    public String getNome() {
        return nome;
    }

    public TipoAplicavelCategoria getTipoAplicavel() {
        return tipoAplicavel;
    }

    public boolean isAtiva() {
        return ativa;
    }
}
