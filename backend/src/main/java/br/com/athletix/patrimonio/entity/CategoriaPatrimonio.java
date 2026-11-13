package br.com.athletix.patrimonio.entity;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.shared.persistence.AuditableEntity;
import br.com.athletix.shared.tenancy.AtleticaScoped;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "categorias_patrimonio", uniqueConstraints = @UniqueConstraint(
        name = "uk_categoria_patrimonio_atletica_nome_normalizado", columnNames = {"atletica_id", "nome_normalizado"}
))
public class CategoriaPatrimonio extends AuditableEntity implements AtleticaScoped {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "atletica_id", nullable = false, foreignKey = @ForeignKey(name = "fk_categoria_patrimonio_atletica"))
    private Atletica atletica;
    @Column(nullable = false, length = 100) private String nome;
    @Column(name = "nome_normalizado", nullable = false, length = 100) private String nomeNormalizado;
    @Column(nullable = false) private boolean ativa;

    protected CategoriaPatrimonio() { }
    public CategoriaPatrimonio(Atletica atletica, String nome, String nomeNormalizado, boolean ativa) {
        this.atletica = atletica; this.nome = nome; this.nomeNormalizado = nomeNormalizado; this.ativa = ativa;
    }
    @Override public Long getAtleticaId() { return atletica.getId(); }
    public String getNome() { return nome; }
    public boolean isAtiva() { return ativa; }
}
