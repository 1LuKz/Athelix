package br.com.athletix.atletica.entity;

import br.com.athletix.shared.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** Organização locatária do Athletix. */
@Entity
@Table(name = "atleticas")
public class Atletica extends AuditableEntity {

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 20)
    private String sigla;

    @Column(length = 160)
    private String instituicao;

    @Column(unique = true, length = 14)
    private String cnpj;

    @Column(name = "fuso_horario", nullable = false, length = 60)
    private String fusoHorario;

    @Column(nullable = false)
    private boolean ativa;

    protected Atletica() {
    }

    public Atletica(
            String nome,
            String sigla,
            String instituicao,
            String cnpj,
            String fusoHorario,
            boolean ativa
    ) {
        this.nome = nome;
        this.sigla = sigla;
        this.instituicao = instituicao;
        this.cnpj = cnpj;
        this.fusoHorario = fusoHorario;
        this.ativa = ativa;
    }

    public String getNome() {
        return nome;
    }

    public String getSigla() {
        return sigla;
    }

    public String getInstituicao() {
        return instituicao;
    }

    public String getCnpj() {
        return cnpj;
    }

    public String getFusoHorario() {
        return fusoHorario;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public void atualizar(
            String nome,
            String sigla,
            String instituicao,
            String cnpj,
            String fusoHorario,
            boolean ativa
    ) {
        this.nome = nome;
        this.sigla = sigla;
        this.instituicao = instituicao;
        this.cnpj = cnpj;
        this.fusoHorario = fusoHorario;
        this.ativa = ativa;
    }
}
