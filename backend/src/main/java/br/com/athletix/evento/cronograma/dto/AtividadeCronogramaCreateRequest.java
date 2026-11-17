package br.com.athletix.evento.cronograma.dto;

import br.com.athletix.evento.cronograma.entity.StatusAtividadeCronograma;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public record AtividadeCronogramaCreateRequest(
    @NotBlank @Size(max = 160) String titulo,
    @Size(max = 1000) String descricao,
    @NotNull OffsetDateTime inicio,
    OffsetDateTime fim,
    StatusAtividadeCronograma status,
    @NotNull @Min(0) Integer ordem) {
  @AssertTrue(message = "O fim da atividade não pode ser anterior ao início.")
  public boolean isPeriodoValido() {
    return inicio == null || fim == null || !fim.isBefore(inicio);
  }
}
