package br.com.athletix.patrimonio.emprestimo.dto;

import jakarta.validation.constraints.*;
import java.time.Instant;

public record EmprestimoPatrimonioCreateRequest(
    @NotNull @Positive Long itemId,
    @NotNull @Positive Long membroResponsavelId,
    @Positive int quantidade,
    Instant devolucaoPrevistaEm,
    @Size(max = 1000) String observacao) {}
