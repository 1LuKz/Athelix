package br.com.athletix.gestao.transicao.dto;

import br.com.athletix.gestao.transicao.entity.*;
import java.time.*;
import java.util.*;

public record TransicaoResponse(
    Long id,
    Long gestaoOrigemId,
    Long gestaoDestinoId,
    StatusTransicaoGestao status,
    Long preparadaPorMembroId,
    Long concluidaPorMembroId,
    Instant concluidaEm,
    Long version,
    List<Item> itens) {
  public record Item(
      Long membroId,
      Long cargoAtualId,
      Long departamentoAtualId,
      Long cargoDestinoId,
      Long departamentoDestinoId,
      boolean continuaAtivo,
      Long substituiResponsavelMembroId) {}

  public static TransicaoResponse from(
      TransicaoGestao transicao, List<ItemTransicaoGestao> itensTransicao) {
    return new TransicaoResponse(
        transicao.getId(),
        transicao.getGestaoOrigemId(),
        transicao.getGestaoDestinoId(),
        transicao.getStatus(),
        transicao.getPreparadaPorMembroId(),
        transicao.getConcluidaPorMembroId(),
        transicao.getConcluidaEm(),
        transicao.getVersion(),
        itensTransicao.stream()
            .map(
                itemTransicao ->
                    new Item(
                        itemTransicao.getMembroId(),
                        itemTransicao.getCargoAtualId(),
                        itemTransicao.getDepartamentoAtualId(),
                        itemTransicao.getCargoDestinoId(),
                        itemTransicao.getDepartamentoDestinoId(),
                        itemTransicao.isContinuaAtivo(),
                        itemTransicao.getSubstituiResponsavelMembroId()))
            .toList());
  }
}
