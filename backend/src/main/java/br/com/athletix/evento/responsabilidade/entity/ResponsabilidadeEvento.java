package br.com.athletix.evento.responsabilidade.entity;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.evento.entity.Evento;
import br.com.athletix.membro.entity.Membro;
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

import java.time.Instant;

/** Designação versionada: encerrar uma área preserva seu responsável anterior. */
@Entity
@Table(name = "responsabilidades_evento")
public class ResponsabilidadeEvento extends AuditableEntity implements AtleticaScoped {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "atletica_id", nullable = false, foreignKey = @ForeignKey(name = "fk_responsabilidade_evento_atletica"))
    private Atletica atletica;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evento_id", nullable = false, foreignKey = @ForeignKey(name = "fk_responsabilidade_evento_evento"))
    private Evento evento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "membro_id", nullable = false, foreignKey = @ForeignKey(name = "fk_responsabilidade_evento_membro"))
    private Membro membro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private AreaResponsabilidadeEvento area;

    @Column(nullable = false)
    private boolean ativa;

    @Column(name = "encerrada_em")
    private Instant encerradaEm;

    @Column(name = "encerrada_por_membro_id")
    private Long encerradaPorMembroId;

    protected ResponsabilidadeEvento() {
    }

    public ResponsabilidadeEvento(Atletica atletica, Evento evento, Membro membro, AreaResponsabilidadeEvento area) {
        this.atletica = atletica;
        this.evento = evento;
        this.membro = membro;
        this.area = area;
        this.ativa = true;
    }

    @Override
    public Long getAtleticaId() { return atletica.getId(); }
    public Long getEventoId() { return evento.getId(); }
    public Long getMembroId() { return membro.getId(); }
    public String getNomeMembro() { return membro.getNomeUsuario(); }
    public AreaResponsabilidadeEvento getArea() { return area; }
    public boolean isAtiva() { return ativa; }
    public Instant getEncerradaEm() { return encerradaEm; }
    public Long getEncerradaPorMembroId() { return encerradaPorMembroId; }

    public void encerrar(Membro autor, Instant instante) {
        this.ativa = false;
        this.encerradaEm = instante;
        this.encerradaPorMembroId = autor.getId();
    }
}
