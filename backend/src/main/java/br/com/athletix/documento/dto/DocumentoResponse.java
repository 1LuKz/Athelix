package br.com.athletix.documento.dto;

import br.com.athletix.documento.entity.*;
import java.time.Instant;

public record DocumentoResponse(
    Long id,
    String titulo,
    CategoriaDocumento categoria,
    String descricao,
    VisibilidadeDocumento visibilidade,
    Long gestaoOrigemId,
    boolean arquivado,
    Instant criadaEm,
    Instant atualizadaEm,
    Long version,
    VersaoDocumentoResponse versaoAtual) {
  public static DocumentoResponse from(Documento documento, VersaoDocumentoResponse versaoAtual) {
    return new DocumentoResponse(
        documento.getId(),
        documento.getTitulo(),
        documento.getCategoria(),
        documento.getDescricao(),
        documento.getVisibilidade(),
        documento.getGestaoOrigemId(),
        documento.isArquivado(),
        documento.getCreatedAt(),
        documento.getUpdatedAt(),
        documento.getVersion(),
        versaoAtual);
  }
}
