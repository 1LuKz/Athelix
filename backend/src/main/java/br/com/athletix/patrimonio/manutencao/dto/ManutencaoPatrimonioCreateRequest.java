package br.com.athletix.patrimonio.manutencao.dto;
import jakarta.validation.constraints.*;
import java.time.Instant;
public record ManutencaoPatrimonioCreateRequest(@NotNull @Positive Long itemId, @Positive int quantidade,
        @NotBlank @Size(max = 1000) String motivo, Instant previsaoConclusaoEm) { }
