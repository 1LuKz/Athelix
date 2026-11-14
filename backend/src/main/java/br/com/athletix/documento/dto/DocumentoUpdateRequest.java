package br.com.athletix.documento.dto;

import br.com.athletix.documento.entity.*;
import jakarta.validation.constraints.*;

public record DocumentoUpdateRequest(
        @NotBlank @Size(max = 200) String titulo,
        @NotNull CategoriaDocumento categoria,
        @Size(max = 2000) String descricao,
        @NotNull VisibilidadeDocumento visibilidade,
        @Positive Long gestaoOrigemId,
        boolean arquivado,
        @NotNull @PositiveOrZero Long version) { }
