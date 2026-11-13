package br.com.athletix.patrimonio.emprestimo.entity;

import br.com.athletix.patrimonio.entity.ConservacaoPatrimonio;
import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "devolucoes_patrimonio")
public class DevolucaoPatrimonio {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "emprestimo_id", nullable = false) private EmprestimoPatrimonio emprestimo;
    @Column(nullable = false) private int quantidade;
    @Column(name = "devolvida_em", nullable = false) private Instant devolvidaEm;
    @Enumerated(EnumType.STRING) @Column(name = "condicao_retorno", nullable = false, length = 8) private ConservacaoPatrimonio condicaoRetorno;
    @Column(length = 1000) private String observacao;
    @Column(name = "registrado_por_membro_id", nullable = false) private Long registradoPorMembroId;
    protected DevolucaoPatrimonio() { }
    public DevolucaoPatrimonio(EmprestimoPatrimonio emprestimo, int quantidade, Instant devolvidaEm,
            ConservacaoPatrimonio condicaoRetorno, String observacao, Long registradoPorMembroId) {
        this.emprestimo = emprestimo; this.quantidade = quantidade; this.devolvidaEm = devolvidaEm;
        this.condicaoRetorno = condicaoRetorno; this.observacao = observacao; this.registradoPorMembroId = registradoPorMembroId;
    }
    public Long getId() { return id; } public Long getEmprestimoId() { return emprestimo.getId(); } public int getQuantidade() { return quantidade; }
    public Instant getDevolvidaEm() { return devolvidaEm; } public ConservacaoPatrimonio getCondicaoRetorno() { return condicaoRetorno; }
    public String getObservacao() { return observacao; } public Long getRegistradoPorMembroId() { return registradoPorMembroId; }
}
