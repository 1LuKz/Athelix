package br.com.athletix.arquivo.dto;

import java.time.Instant;

/** Metadados internos, deliberadamente sem conteúdo binário. */
public record ArquivoMetadata(
    Long id,
    Long atleticaId,
    String nomeOriginal,
    String mimeType,
    long tamanhoBytes,
    String hashConteudo,
    Long enviadoPorMembroId,
    Instant enviadoEm) {}
