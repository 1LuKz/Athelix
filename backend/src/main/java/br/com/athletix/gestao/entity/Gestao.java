package br.com.athletix.gestao.entity;

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

import java.time.Instant;
import java.time.LocalDate;

/** Ciclo administrativo da atlética; a transição pública será exposta no fluxo de sucessão. */
@Entity
@Table(name = "gestoes")
public class Gestao extends AuditableEntity implements AtleticaScoped {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "atletica_id", nullable = false, foreignKey = @ForeignKey(name = "fk_gestao_atletica"))
    private Atletica atletica;

    @Column(nullable = false, length = 80)
    private String nome;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim_prevista", nullable = false)
    private LocalDate dataFimPrevista;

    @Column(name = "encerrada_em")
    private Instant encerradaEm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StatusGestao status;

    protected Gestao() {
    }

    public Gestao(Atletica atletica, String nome, LocalDate dataInicio, LocalDate dataFimPrevista) {
        this.atletica = atletica;
        this.nome = nome;
        this.dataInicio = dataInicio;
        this.dataFimPrevista = dataFimPrevista;
        this.status = StatusGestao.PLANEJADA;
    }

    @Override
    public Long getAtleticaId() {
        return atletica.getId();
    }

    public String getNome() {
        return nome;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public LocalDate getDataFimPrevista() {
        return dataFimPrevista;
    }

    public Instant getEncerradaEm() {
        return encerradaEm;
    }

    public StatusGestao getStatus() {
        return status;
    }

    public void ativar() {
        this.status = StatusGestao.ATIVA;
        this.encerradaEm = null;
    }

    public void encerrar(Instant instante) {
        this.status = StatusGestao.ENCERRADA;
        this.encerradaEm = instante;
    }
}
