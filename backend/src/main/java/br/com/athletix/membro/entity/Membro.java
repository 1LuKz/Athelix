package br.com.athletix.membro.entity;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.cargo.entity.Cargo;
import br.com.athletix.cargo.entity.PerfilAcesso;
import br.com.athletix.departamento.entity.Departamento;
import br.com.athletix.shared.persistence.AuditableEntity;
import br.com.athletix.shared.tenancy.AtleticaScoped;
import br.com.athletix.usuario.entity.Usuario;
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

import java.time.Instant;
import java.time.LocalDate;

/** Vínculo de uma identidade global com uma atlética. */
@Entity
@Table(
        name = "membros",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_membro_usuario_atletica",
                columnNames = {"usuario_id", "atletica_id"}
        )
)
public class Membro extends AuditableEntity implements AtleticaScoped {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "atletica_id", nullable = false, foreignKey = @ForeignKey(name = "fk_membro_atletica"))
    private Atletica atletica;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, foreignKey = @ForeignKey(name = "fk_membro_usuario"))
    private Usuario usuario;

    @Column(nullable = false, length = 160)
    private String curso;

    @Column(name = "periodo_curso")
    private Integer periodoCurso;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cargo_id", nullable = false, foreignKey = @ForeignKey(name = "fk_membro_cargo"))
    private Cargo cargo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "departamento_id", nullable = false, foreignKey = @ForeignKey(name = "fk_membro_departamento"))
    private Departamento departamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StatusMembro status;

    @Column(name = "membro_desde", nullable = false)
    private LocalDate membroDesde;

    @Column(name = "ativado_em")
    private Instant ativadoEm;

    @Column(name = "inativado_em")
    private Instant inativadoEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validado_por_membro_id", foreignKey = @ForeignKey(name = "fk_membro_validador"))
    private Membro validadoPorMembro;

    protected Membro() {
    }

    public Membro(Atletica atletica, Usuario usuario, String curso, Integer periodoCurso, Cargo cargo,
                  Departamento departamento, LocalDate membroDesde) {
        this.atletica = atletica;
        this.usuario = usuario;
        this.curso = curso;
        this.periodoCurso = periodoCurso;
        this.cargo = cargo;
        this.departamento = departamento;
        this.membroDesde = membroDesde;
        this.status = StatusMembro.PENDENTE;
    }

    @Override
    public Long getAtleticaId() {
        return atletica.getId();
    }

    public Long getUsuarioId() {
        return usuario.getId();
    }

    public String getNomeUsuario() {
        return usuario.getNome();
    }

    public String getEmailUsuario() {
        return usuario.getEmail();
    }

    public String getTelefoneUsuario() {
        return usuario.getTelefone();
    }

    public String getCurso() {
        return curso;
    }

    public Integer getPeriodoCurso() {
        return periodoCurso;
    }

    public Long getCargoId() {
        return cargo.getId();
    }

    public String getNomeCargo() {
        return cargo.getNome();
    }

    public PerfilAcesso getPerfilAcesso() {
        return cargo.getPerfilAcesso();
    }

    public boolean cargoAtivo() {
        return cargo.isAtivo();
    }

    public boolean atleticaAtiva() {
        return atletica.isAtiva();
    }

    public String getNomeAtletica() {
        return atletica.getNome();
    }

    public Long getDepartamentoId() {
        return departamento.getId();
    }

    public String getNomeDepartamento() {
        return departamento.getNome();
    }

    public StatusMembro getStatus() {
        return status;
    }

    public LocalDate getMembroDesde() {
        return membroDesde;
    }

    public Instant getAtivadoEm() {
        return ativadoEm;
    }

    public Instant getInativadoEm() {
        return inativadoEm;
    }

    public Long getValidadoPorMembroId() {
        return validadoPorMembro == null ? null : validadoPorMembro.getId();
    }

    public void ativar(Membro validador) {
        this.status = StatusMembro.ATIVO;
        this.ativadoEm = Instant.now();
        this.inativadoEm = null;
        this.validadoPorMembro = validador;
    }

    public void inativar() {
        this.status = StatusMembro.INATIVO;
        this.inativadoEm = Instant.now();
    }

    public void atualizarVinculo(String curso, Integer periodoCurso, Cargo cargo, Departamento departamento) {
        this.curso = curso;
        this.periodoCurso = periodoCurso;
        this.cargo = cargo;
        this.departamento = departamento;
    }
}
