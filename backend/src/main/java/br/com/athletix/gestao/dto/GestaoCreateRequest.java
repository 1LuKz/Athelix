package br.com.athletix.gestao.dto;

import br.com.athletix.shared.api.dto.CreateRequest;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record GestaoCreateRequest(
    @NotBlank @Size(max = 80) String nome,
    @NotNull LocalDate dataInicio,
    @NotNull LocalDate dataFimPrevista)
    implements CreateRequest {
  @AssertTrue(message = "A data de início não pode ser posterior ao fim previsto.")
  public boolean isPeriodoValido() {
    return dataInicio == null || dataFimPrevista == null || !dataInicio.isAfter(dataFimPrevista);
  }
}
