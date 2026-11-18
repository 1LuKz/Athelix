package br.com.athletix.patrimonio.conferencia.dto;

import br.com.athletix.patrimonio.conferencia.entity.*;
import java.time.Instant;

public record ConferenciaResponse(
    Long id,
    Long atleticaId,
    Long gestaoId,
    String nome,
    Instant iniciadaEm,
    Instant encerradaEm,
    StatusConferenciaInventario status,
    Long criadaPorMembroId,
    Long version) {
  public static ConferenciaResponse from(ConferenciaInventario conferencia) {
    return new ConferenciaResponse(
        conferencia.getId(),
        conferencia.getAtleticaId(),
        conferencia.getGestaoId(),
        conferencia.getNome(),
        conferencia.getIniciadaEm(),
        conferencia.getEncerradaEm(),
        conferencia.getStatus(),
        conferencia.getCriadaPorMembroId(),
        conferencia.getVersion());
  }
}
