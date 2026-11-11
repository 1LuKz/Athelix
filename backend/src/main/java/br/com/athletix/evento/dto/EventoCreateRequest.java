package br.com.athletix.evento.dto;

import br.com.athletix.shared.api.dto.CreateRequest;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record EventoCreateRequest(
        @NotBlank @Size(max = 160) String nome,
        @Size(max = 5000) String descricao,
        @NotNull OffsetDateTime inicio,
        @NotNull OffsetDateTime fim,
        @NotBlank @Size(max = 200) String local,
        @Size(max = 120) String cidade,
        @Pattern(regexp = "(?i)^[A-Z]{2}$", message = "A UF deve conter duas letras.") String uf,
        @NotBlank String orcamentoPrevisto,
        @PositiveOrZero Integer participantesEsperados
) implements CreateRequest {
    @AssertTrue(message = "O fim do evento deve ser igual ou posterior ao início.")
    public boolean isPeriodoValido() {
        return inicio == null || fim == null || !fim.isBefore(inicio);
    }
}
