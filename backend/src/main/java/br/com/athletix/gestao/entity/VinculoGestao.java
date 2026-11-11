package br.com.athletix.gestao.entity;

import br.com.athletix.cargo.entity.Cargo;
import br.com.athletix.departamento.entity.Departamento;
import br.com.athletix.membro.entity.Membro;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import java.time.Instant;

/** Período imutável de cargo e departamento de um membro dentro de uma gestão. */
@Entity
@Table(name = "vinculos_gestao", uniqueConstraints = @UniqueConstraint(
        name = "uk_vinculo_gestao_inicio", columnNames = {"membro_id", "gestao_id", "inicio_vigencia"}
))
public class VinculoGestao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "membro_id", nullable = false, foreignKey = @ForeignKey(name = "fk_vinculo_gestao_membro"))
    private Membro membro;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "gestao_id", nullable = false, foreignKey = @ForeignKey(name = "fk_vinculo_gestao_gestao"))
    private Gestao gestao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cargo_id", nullable = false, foreignKey = @ForeignKey(name = "fk_vinculo_gestao_cargo"))
    private Cargo cargo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "departamento_id", nullable = false, foreignKey = @ForeignKey(name = "fk_vinculo_gestao_departamento"))
    private Departamento departamento;

    @Column(name = "inicio_vigencia", nullable = false, updatable = false)
    private Instant inicioVigencia;

    @Column(name = "fim_vigencia")
    private Instant fimVigencia;

    @Version
    private Long version;

    protected VinculoGestao() {
    }

    public VinculoGestao(Membro membro, Gestao gestao, Cargo cargo, Departamento departamento, Instant inicioVigencia) {
        this.membro = membro;
        this.gestao = gestao;
        this.cargo = cargo;
        this.departamento = departamento;
        this.inicioVigencia = inicioVigencia;
    }

    public Long getId() {
        return id;
    }

    public Long getMembroId() {
        return membro.getId();
    }

    public Long getGestaoId() {
        return gestao.getId();
    }

    public Long getCargoId() {
        return cargo.getId();
    }

    public Long getDepartamentoId() {
        return departamento.getId();
    }

    public Instant getInicioVigencia() {
        return inicioVigencia;
    }

    public Instant getFimVigencia() {
        return fimVigencia;
    }

    public Long getVersion() {
        return version;
    }

    public void encerrar(Instant instante) {
        if (fimVigencia != null) {
            throw new IllegalStateException("O vínculo de gestão já foi encerrado.");
        }
        this.fimVigencia = instante;
    }
}
