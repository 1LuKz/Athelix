package br.com.athletix.documento.entity;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.shared.persistence.AuditableEntity;
import br.com.athletix.shared.tenancy.AtleticaScoped;
import jakarta.persistence.*;

@Entity
@Table(name = "documentos")
public class Documento extends AuditableEntity implements AtleticaScoped {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "atletica_id", nullable = false)
    private Atletica atletica;
    @Column(nullable = false, length = 200) private String titulo;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 12) private CategoriaDocumento categoria;
    @Column(length = 2000) private String descricao;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 12) private VisibilidadeDocumento visibilidade;
    @Column(name = "gestao_origem_id") private Long gestaoOrigemId;
    @Column(nullable = false) private boolean arquivado;

    protected Documento() { }
    public Documento(Atletica atletica, String titulo, CategoriaDocumento categoria, String descricao,
                     VisibilidadeDocumento visibilidade, Long gestaoOrigemId) {
        this.atletica = atletica; this.titulo = titulo; this.categoria = categoria; this.descricao = descricao;
        this.visibilidade = visibilidade; this.gestaoOrigemId = gestaoOrigemId;
    }
    @Override public Long getAtleticaId() { return atletica.getId(); }
    public String getTitulo() { return titulo; }
    public CategoriaDocumento getCategoria() { return categoria; }
    public String getDescricao() { return descricao; }
    public VisibilidadeDocumento getVisibilidade() { return visibilidade; }
    public Long getGestaoOrigemId() { return gestaoOrigemId; }
    public boolean isArquivado() { return arquivado; }
    public void atualizar(String titulo, CategoriaDocumento categoria, String descricao, VisibilidadeDocumento visibilidade,
                          Long gestaoOrigemId, boolean arquivado) {
        this.titulo = titulo; this.categoria = categoria; this.descricao = descricao; this.visibilidade = visibilidade;
        this.gestaoOrigemId = gestaoOrigemId; this.arquivado = arquivado;
    }
}
