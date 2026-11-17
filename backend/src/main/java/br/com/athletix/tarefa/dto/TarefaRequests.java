package br.com.athletix.tarefa.dto;

import br.com.athletix.tarefa.entity.*;
import jakarta.validation.constraints.*;
import java.time.*;

public final class TarefaRequests {
  private TarefaRequests() {}

  public record Create(
      @NotNull @Positive Long eventoId,
      @NotBlank @Size(max = 160) String titulo,
      @Size(max = 3000) String descricao,
      @Positive Long responsavelMembroId,
      @Positive Long departamentoId,
      @NotNull PrioridadeTarefa prioridade,
      LocalDate prazo) {}

  public record Update(
      @NotBlank @Size(max = 160) String titulo,
      @Size(max = 3000) String descricao,
      @Positive Long responsavelMembroId,
      @Positive Long departamentoId,
      @NotNull PrioridadeTarefa prioridade,
      LocalDate prazo,
      @NotNull @PositiveOrZero Long version) {}

  public record Status(
      @NotNull StatusTarefa status,
      @Size(max = 500) String motivo,
      @NotNull @PositiveOrZero Long version) {}
}
