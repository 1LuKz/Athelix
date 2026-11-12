package br.com.athletix.arquivo.entity;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.shared.tenancy.AtleticaScoped;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

/** Conteúdo binário privado; metadados e BLOB são consultados separadamente. */
@Entity
@Table(name = "arquivos")
public class Arquivo implements AtleticaScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "atletica_id", nullable = false, foreignKey = @ForeignKey(name = "fk_arquivo_atletica"))
    private Atletica atletica;

    @Column(name = "nome_original", nullable = false, length = 255)
    private String nomeOriginal;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "tamanho_bytes", nullable = false)
    private long tamanhoBytes;

    @Column(name = "hash_conteudo", nullable = false, length = 64)
    private String hashConteudo;

    @Column(name = "enviado_por_membro_id", nullable = false)
    private Long enviadoPorMembroId;

    @Column(name = "enviado_em", nullable = false, updatable = false)
    private Instant enviadoEm;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @JsonIgnore
    @Column(nullable = false)
    private byte[] conteudo;

    protected Arquivo() {
    }

    public Arquivo(Atletica atletica, String nomeOriginal, String mimeType, long tamanhoBytes,
                   String hashConteudo, Long enviadoPorMembroId, Instant enviadoEm, byte[] conteudo) {
        this.atletica = atletica;
        this.nomeOriginal = nomeOriginal;
        this.mimeType = mimeType;
        this.tamanhoBytes = tamanhoBytes;
        this.hashConteudo = hashConteudo;
        this.enviadoPorMembroId = enviadoPorMembroId;
        this.enviadoEm = enviadoEm;
        this.conteudo = conteudo;
    }

    public Long getId() { return id; }
    @Override public Long getAtleticaId() { return atletica.getId(); }
    public String getNomeOriginal() { return nomeOriginal; }
    public String getMimeType() { return mimeType; }
    public long getTamanhoBytes() { return tamanhoBytes; }
    public String getHashConteudo() { return hashConteudo; }
    public Long getEnviadoPorMembroId() { return enviadoPorMembroId; }
    public Instant getEnviadoEm() { return enviadoEm; }
}
