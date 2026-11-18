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
  public static ItemConferenciaResponse from(ItemConferencia itemConferencia) {
    return new ItemConferenciaResponse(
        itemConferencia.getId(),
        itemConferencia.getItemId(),
        itemConferencia.getQuantidadeEsperada(),
        itemConferencia.getQuantidadeVerificada(),
        itemConferencia.getDivergencia(),
        itemConferencia.getObservacao(),
        itemConferencia.getConferidaPorMembroId(),
        itemConferencia.getConferidaEm(),
        itemConferencia.getVersion());
  }
}
