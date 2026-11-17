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
  public static DocumentoResponse from(Documento d, VersaoDocumentoResponse atual) {
    return new DocumentoResponse(
        d.getId(),
        d.getTitulo(),
        d.getCategoria(),
        d.getDescricao(),
        d.getVisibilidade(),
        d.getGestaoOrigemId(),
        d.isArquivado(),
        d.getCreatedAt(),
        d.getUpdatedAt(),
        d.getVersion(),
        atual);
  }
}
