package br.com.athletix.documento.entity;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.shared.tenancy.AtleticaScoped;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "versoes_documento",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_versao_documento_numero",
            columnNames = {"documento_id", "numero_versao"}))
public class VersaoDocumento implements AtleticaScoped {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "atletica_id", nullable = false)
  private Atletica atletica;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "documento_id", nullable = false)
  private Documento documento;

  @Column(name = "numero_versao", nullable = false)
  private int numeroVersao;

  @Column(name = "arquivo_id", nullable = false)
  private Long arquivoId;

  @Column(name = "gestao_id")
  private Long gestaoId;

  @Column(length = 1000)
  private String observacao;

  @Column(name = "enviado_por_membro_id", nullable = false)
  private Long enviadoPorMembroId;

  @Column(name = "enviado_em", nullable = false, updatable = false)
  private Instant enviadoEm;

  protected VersaoDocumento() {}

  public VersaoDocumento(
      Atletica atletica,
      Documento documento,
      int numeroVersao,
      Long arquivoId,
      Long gestaoId,
      String observacao,
      Long enviadoPorMembroId,
      Instant enviadoEm) {
    this.atletica = atletica;
    this.documento = documento;
    this.numeroVersao = numeroVersao;
    this.arquivoId = arquivoId;
    this.gestaoId = gestaoId;
    this.observacao = observacao;
    this.enviadoPorMembroId = enviadoPorMembroId;
    this.enviadoEm = enviadoEm;
  }

  public Long getId() {
    return id;
  }

  @Override
  public Long getAtleticaId() {
    return atletica.getId();
  }

  public Long getDocumentoId() {
    return documento.getId();
  }

  public int getNumeroVersao() {
    return numeroVersao;
  }

  public Long getArquivoId() {
    return arquivoId;
  }

  public Long getGestaoId() {
    return gestaoId;
  }

  public String getObservacao() {
    return observacao;
  }

  public Long getEnviadoPorMembroId() {
    return enviadoPorMembroId;
  }

  public Instant getEnviadoEm() {
    return enviadoEm;
  }
}
