package br.com.athletix.documento.dto;

import br.com.athletix.documento.entity.VersaoDocumento;
import java.time.Instant;

public record VersaoDocumentoResponse(
    Long id,
    int numeroVersao,
    Long arquivoId,
    String nomeArquivo,
    Long gestaoId,
    String observacao,
    Long enviadoPorMembroId,
    Instant enviadoEm) {
  public static VersaoDocumentoResponse from(VersaoDocumento versaoDocumento, String nomeArquivo) {
    return new VersaoDocumentoResponse(
        versaoDocumento.getId(),
        versaoDocumento.getNumeroVersao(),
        versaoDocumento.getArquivoId(),
        nomeArquivo,
        versaoDocumento.getGestaoId(),
        versaoDocumento.getObservacao(),
        versaoDocumento.getEnviadoPorMembroId(),
        versaoDocumento.getEnviadoEm());
  }
}
