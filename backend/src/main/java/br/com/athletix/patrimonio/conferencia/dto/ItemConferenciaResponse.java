package br.com.athletix.patrimonio.conferencia.dto;

import br.com.athletix.patrimonio.conferencia.entity.ItemConferencia;
import java.time.Instant;

public record ItemConferenciaResponse(
    Long id,
    Long itemId,
    int quantidadeEsperada,
    Integer quantidadeVerificada,
    Integer divergencia,
    String observacao,
    Long conferidaPorMembroId,
    Instant conferidaEm,
    Long version) {
  public static ItemConferenciaResponse from(ItemConferencia i) {
    return new ItemConferenciaResponse(
        i.getId(),
        i.getItemId(),
        i.getQuantidadeEsperada(),
        i.getQuantidadeVerificada(),
        i.getDivergencia(),
        i.getObservacao(),
        i.getConferidaPorMembroId(),
        i.getConferidaEm(),
        i.getVersion());
  }
}
