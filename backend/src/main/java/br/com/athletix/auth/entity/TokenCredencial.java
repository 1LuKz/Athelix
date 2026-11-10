package br.com.athletix.auth.entity;

import br.com.athletix.membro.entity.Membro;
import br.com.athletix.shared.persistence.AuditableEntity;
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

import java.time.Instant;

/** Token de uso único. Apenas o resumo SHA-256 do segredo é persistido. */
@Entity
@Table(name = "tokens_credencial")
public class TokenCredencial extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, foreignKey = @ForeignKey(name = "fk_token_credencial_usuario"))
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membro_id", foreignKey = @ForeignKey(name = "fk_token_credencial_membro"))
    private Membro membro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FinalidadeTokenCredencial finalidade;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "expira_em", nullable = false)
    private Instant expiraEm;

    @Column(name = "consumido_em")
    private Instant consumidoEm;

    protected TokenCredencial() {
    }

    public TokenCredencial(Usuario usuario, Membro membro, FinalidadeTokenCredencial finalidade,
                           String tokenHash, Instant expiraEm) {
        this.usuario = usuario;
        this.membro = membro;
        this.finalidade = finalidade;
        this.tokenHash = tokenHash;
        this.criadoEm = Instant.now();
        this.expiraEm = expiraEm;
    }

    public Long getUsuarioId() {
        return usuario.getId();
    }

    public Long getMembroId() {
        return membro == null ? null : membro.getId();
    }

    public FinalidadeTokenCredencial getFinalidade() {
        return finalidade;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getExpiraEm() {
        return expiraEm;
    }

    public Instant getConsumidoEm() {
        return consumidoEm;
    }

    public boolean estaDisponivelEm(Instant instante) {
        return consumidoEm == null && expiraEm.isAfter(instante);
    }

    public void consumir(Instant instante) {
        this.consumidoEm = instante;
    }
}
