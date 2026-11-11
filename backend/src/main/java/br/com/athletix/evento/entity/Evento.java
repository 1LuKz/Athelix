package br.com.athletix.evento.entity;

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

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Evento da atlética; origem e autoria são escalares para preservar o registro histórico. */
@Entity
@Table(name = "eventos")
public class Evento extends AuditableEntity implements AtleticaScoped {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "atletica_id", nullable = false, foreignKey = @ForeignKey(name = "fk_evento_atletica"))
    private Atletica atletica;

    @Column(name = "gestao_origem_id")
    private Long gestaoOrigemId;

    @Column(nullable = false, length = 160)
    private String nome;

    @Column(length = 5000)
    private String descricao;

    @Column(nullable = false)
    private OffsetDateTime inicio;

    @Column(nullable = false)
    private OffsetDateTime fim;

    @Column(nullable = false, length = 200)
    private String local;

    @Column(length = 120)
    private String cidade;

    @Column(length = 2)
    private String uf;

    @Column(name = "orcamento_previsto", nullable = false, precision = 19, scale = 2)
    private BigDecimal orcamentoPrevisto;

    @Column(name = "participantes_esperados")
    private Integer participantesEsperados;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private StatusEvento status;

    @Column(name = "criado_por_membro_id", nullable = false)
    private Long criadoPorMembroId;

    protected Evento() {
    }

    public Evento(Atletica atletica, Long gestaoOrigemId, String nome, String descricao, OffsetDateTime inicio,
                  OffsetDateTime fim, String local, String cidade, String uf, BigDecimal orcamentoPrevisto,
                  Integer participantesEsperados, Long criadoPorMembroId) {
        this.atletica = atletica;
        this.gestaoOrigemId = gestaoOrigemId;
        this.nome = nome;
        this.descricao = descricao;
        this.inicio = inicio;
        this.fim = fim;
        this.local = local;
        this.cidade = cidade;
        this.uf = uf;
        this.orcamentoPrevisto = orcamentoPrevisto;
        this.participantesEsperados = participantesEsperados;
        this.criadoPorMembroId = criadoPorMembroId;
        this.status = StatusEvento.PLANEJADO;
    }

    @Override
    public Long getAtleticaId() {
        return atletica.getId();
    }

    public String getFusoHorarioAtletica() {
        return atletica.getFusoHorario();
    }

    public Long getGestaoOrigemId() { return gestaoOrigemId; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public OffsetDateTime getInicio() { return inicio; }
    public OffsetDateTime getFim() { return fim; }
    public String getLocal() { return local; }
    public String getCidade() { return cidade; }
    public String getUf() { return uf; }
    public BigDecimal getOrcamentoPrevisto() { return orcamentoPrevisto; }
    public Integer getParticipantesEsperados() { return participantesEsperados; }
    public StatusEvento getStatus() { return status; }
    public Long getCriadoPorMembroId() { return criadoPorMembroId; }

    public void atualizarDados(String nome, String descricao, OffsetDateTime inicio, OffsetDateTime fim, String local,
                               String cidade, String uf, BigDecimal orcamentoPrevisto, Integer participantesEsperados) {
        this.nome = nome;
        this.descricao = descricao;
        this.inicio = inicio;
        this.fim = fim;
        this.local = local;
        this.cidade = cidade;
        this.uf = uf;
        this.orcamentoPrevisto = orcamentoPrevisto;
        this.participantesEsperados = participantesEsperados;
    }

    public void alterarStatus(StatusEvento novoStatus) {
        this.status = novoStatus;
    }
}
