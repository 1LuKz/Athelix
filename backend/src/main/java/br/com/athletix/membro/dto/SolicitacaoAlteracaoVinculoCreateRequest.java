package br.com.athletix.membro.dto;

import br.com.athletix.shared.api.dto.CreateRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record SolicitacaoAlteracaoVinculoCreateRequest(
        @NotNull @Positive Long cargoPretendidoId,
        @NotNull @Positive Long departamentoPretendidoId,
        @NotBlank @Size(max = 500) String motivo
) implements CreateRequest { }
