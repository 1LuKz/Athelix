package br.com.athletix.patrimonio.entity;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.departamento.entity.Departamento;
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

import java.math.BigDecimal;

/** Um item representa somente unidades homogêneas sob a mesma conservação, localização e estimativa. */
@Entity
@Table(name = "itens_patrimonio")
public class ItemPatrimonio extends AuditableEntity implements AtleticaScoped {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "atletica_id", nullable = false, foreignKey = @ForeignKey(name = "fk_item_patrimonio_atletica"))
    private Atletica atletica;
    @Column(name = "gestao_origem_id") private Long gestaoOrigemId;
    @Column(nullable = false, length = 160) private String nome;
    @Column(length = 2000) private String descricao;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false, foreignKey = @ForeignKey(name = "fk_item_patrimonio_categoria"))
    private CategoriaPatrimonio categoria;
    @Column(name = "quantidade_total", nullable = false) private int quantidadeTotal;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 8) private ConservacaoPatrimonio conservacao;
    @Column(nullable = false, length = 200) private String localizacao;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "departamento_responsavel_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_item_patrimonio_departamento"))
    private Departamento departamentoResponsavel;
    @Column(name = "valor_estimado_unitario", nullable = false, precision = 19, scale = 2)
    private BigDecimal valorEstimadoUnitario;
    @Column(nullable = false) private boolean ativo;

    protected ItemPatrimonio() { }
    public ItemPatrimonio(Atletica atletica, Long gestaoOrigemId, String nome, String descricao,
                          CategoriaPatrimonio categoria, int quantidadeTotal, ConservacaoPatrimonio conservacao,
                          String localizacao, Departamento departamentoResponsavel, BigDecimal valorEstimadoUnitario,
                          boolean ativo) {
        this.atletica = atletica; this.gestaoOrigemId = gestaoOrigemId; this.nome = nome; this.descricao = descricao;
        this.categoria = categoria; this.quantidadeTotal = quantidadeTotal; this.conservacao = conservacao;
        this.localizacao = localizacao; this.departamentoResponsavel = departamentoResponsavel;
        this.valorEstimadoUnitario = valorEstimadoUnitario; this.ativo = ativo;
    }
    @Override public Long getAtleticaId() { return atletica.getId(); }
    public Long getGestaoOrigemId() { return gestaoOrigemId; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public Long getCategoriaId() { return categoria.getId(); }
    public String getCategoriaNome() { return categoria.getNome(); }
    public int getQuantidadeTotal() { return quantidadeTotal; }
    public ConservacaoPatrimonio getConservacao() { return conservacao; }
    public String getLocalizacao() { return localizacao; }
    public Long getDepartamentoResponsavelId() { return departamentoResponsavel.getId(); }
    public String getDepartamentoResponsavelNome() { return departamentoResponsavel.getNome(); }
    public BigDecimal getValorEstimadoUnitario() { return valorEstimadoUnitario; }
    public boolean isAtivo() { return ativo; }
    public void atualizar(String nome, String descricao, CategoriaPatrimonio categoria, int quantidadeTotal,
                          ConservacaoPatrimonio conservacao, String localizacao, Departamento departamento,
                          BigDecimal valorEstimadoUnitario, boolean ativo) {
        this.nome = nome; this.descricao = descricao; this.categoria = categoria; this.quantidadeTotal = quantidadeTotal;
        this.conservacao = conservacao; this.localizacao = localizacao; this.departamentoResponsavel = departamento;
        this.valorEstimadoUnitario = valorEstimadoUnitario; this.ativo = ativo;
    }
}
