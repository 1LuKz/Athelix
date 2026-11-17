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
  public static ConferenciaResponse from(ConferenciaInventario c) {
    return new ConferenciaResponse(
        c.getId(),
        c.getAtleticaId(),
        c.getGestaoId(),
        c.getNome(),
        c.getIniciadaEm(),
        c.getEncerradaEm(),
        c.getStatus(),
        c.getCriadaPorMembroId(),
        c.getVersion());
  }
}
