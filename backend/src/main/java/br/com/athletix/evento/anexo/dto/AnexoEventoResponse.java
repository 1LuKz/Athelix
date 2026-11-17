package br.com.athletix.evento.anexo.dto;

import br.com.athletix.evento.anexo.entity.AnexoEvento;
import br.com.athletix.evento.anexo.entity.TipoMaterialEvento;
import java.time.Instant;

/** Metadados do anexo; o BLOB é entregue somente pela leitura protegida do arquivo. */
public record AnexoEventoResponse(
    Long id,
    Long atleticaId,
    Long eventoId,
    Long arquivoId,
    String titulo,
    String descricao,
    TipoMaterialEvento tipoMaterial,
    Instant criadoEm,
    Instant atualizadoEm,
    Long version) {
  public static AnexoEventoResponse from(AnexoEvento anexo) {
    return new AnexoEventoResponse(
        anexo.getId(),
        anexo.getAtleticaId(),
        anexo.getEventoId(),
        anexo.getArquivoId(),
        anexo.getTitulo(),
        anexo.getDescricao(),
        anexo.getTipoMaterial(),
        anexo.getCreatedAt(),
        anexo.getUpdatedAt(),
        anexo.getVersion());
  }
}
