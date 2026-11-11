package br.com.athletix.auditoria.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

/** Registro imutável de ação produzida exclusivamente pelo servidor. */
@Entity
@Table(name = "atividades_usuario", indexes = {
        @Index(name = "ix_atividade_usuario_atletica_ocorrida", columnList = "usuario_id, atletica_id, ocorrida_em")
})
public class AtividadeUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "atletica_id", nullable = false)
    private Long atleticaId;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "membro_id", nullable = false)
    private Long membroId;

    @Column(nullable = false, length = 80)
    private String acao;

    @Column(name = "tipo_recurso", nullable = false, length = 80)
    private String tipoRecurso;

    @Column(name = "recurso_id")
    private Long recursoId;

    @Column(nullable = false, length = 500)
    private String resumo;

    @Column(name = "ocorrida_em", nullable = false, updatable = false)
    private Instant ocorridaEm;

    /** Referência escalar para preservar o histórico mesmo após o fim da gestão. */
    @Column(name = "gestao_id")
    private Long gestaoId;

    protected AtividadeUsuario() {
    }

    public AtividadeUsuario(Long atleticaId, Long usuarioId, Long membroId, String acao, String tipoRecurso,
                            Long recursoId, String resumo, Instant ocorridaEm, Long gestaoId) {
        this.atleticaId = atleticaId;
        this.usuarioId = usuarioId;
        this.membroId = membroId;
        this.acao = acao;
        this.tipoRecurso = tipoRecurso;
        this.recursoId = recursoId;
        this.resumo = resumo;
        this.ocorridaEm = ocorridaEm;
        this.gestaoId = gestaoId;
    }

    public Long getId() { return id; }
    public Long getAtleticaId() { return atleticaId; }
    public Long getUsuarioId() { return usuarioId; }
    public Long getMembroId() { return membroId; }
    public String getAcao() { return acao; }
    public String getTipoRecurso() { return tipoRecurso; }
    public Long getRecursoId() { return recursoId; }
    public String getResumo() { return resumo; }
    public Instant getOcorridaEm() { return ocorridaEm; }
    public Long getGestaoId() { return gestaoId; }
}
