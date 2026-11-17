package br.com.athletix.patrimonio.manutencao.dto;

import br.com.athletix.patrimonio.manutencao.entity.ManutencaoPatrimonio;
import java.time.Instant;

public record ManutencaoPatrimonioResponse(
    Long id,
    Long itemId,
    int quantidade,
    String motivo,
    Instant iniciadaEm,
    Instant previsaoConclusaoEm,
    Instant concluidaEm,
    String resultado,
    Long registradoPorMembroId,
    Instant createdAt,
    Instant updatedAt,
    Long version) {
  public static ManutencaoPatrimonioResponse from(ManutencaoPatrimonio m) {
    return new ManutencaoPatrimonioResponse(
        m.getId(),
        m.getItemId(),
        m.getQuantidade(),
        m.getMotivo(),
        m.getIniciadaEm(),
        m.getPrevisaoConclusaoEm(),
        m.getConcluidaEm(),
        m.getResultado(),
        m.getRegistradoPorMembroId(),
        m.getCreatedAt(),
        m.getUpdatedAt(),
        m.getVersion());
  }
}
