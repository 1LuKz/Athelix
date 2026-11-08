package br.com.athletix.usuario.entity;

import br.com.athletix.shared.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;

/** Identidade global. O vínculo com cada atlética pertence à entidade Membro. */
@Entity
@Table(name = "usuarios")
public class Usuario extends AuditableEntity {

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(length = 20)
    private String telefone;

    /** Nunca é serializado por DTOs de resposta. Pode ser nulo somente durante o convite. */
    @Column(name = "senha_hash", length = 255)
    private String senhaHash;

    @Column(name = "credencial_ativa", nullable = false)
    private boolean credencialAtiva;

    @Column(name = "ultimo_acesso_em")
    private Instant ultimoAcessoEm;

    @Column(name = "senha_alterada_em")
    private Instant senhaAlteradaEm;

    protected Usuario() {
    }

    public Usuario(String nome, String email, String telefone) {
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.credencialAtiva = false;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefone() {
        return telefone;
    }

    public boolean isCredencialAtiva() {
        return credencialAtiva;
    }

    public Instant getUltimoAcessoEm() {
        return ultimoAcessoEm;
    }

    public Instant getSenhaAlteradaEm() {
        return senhaAlteradaEm;
    }

    public boolean possuiCredencialDefinida() {
        return senhaHash != null && !senhaHash.isBlank();
    }

    public void atualizarDados(String nome, String telefone) {
        this.nome = nome;
        this.telefone = telefone;
    }

    public void definirSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
        this.credencialAtiva = true;
        this.senhaAlteradaEm = Instant.now();
    }

    public void desativarCredencial() {
        this.credencialAtiva = false;
    }

    public void registrarAcesso() {
        this.ultimoAcessoEm = Instant.now();
    }
}
