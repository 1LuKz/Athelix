package br.com.athletix.patrimonio.dto;

import br.com.athletix.patrimonio.entity.ConservacaoPatrimonio;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ItemPatrimonioUpdateRequest(
    @NotBlank @Size(max = 160) String nome,
    @Size(max = 2000) String descricao,
    @NotNull @Positive Long categoriaId,
    @Positive int quantidadeTotal,
    @NotNull ConservacaoPatrimonio conservacao,
    @NotBlank @Size(max = 200) String localizacao,
    @NotNull @Positive Long departamentoResponsavelId,
    @NotBlank String valorEstimadoUnitario,
    @NotNull Boolean ativo,
    @Size(max = 500) String justificativaReducao,
    @NotNull @PositiveOrZero Long version) {}
