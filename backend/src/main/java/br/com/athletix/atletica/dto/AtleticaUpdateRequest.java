package br.com.athletix.atletica.dto;

import br.com.athletix.shared.api.dto.UpdateRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record AtleticaUpdateRequest(
    @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.")
        String nome,
    @Size(max = 20, message = "A sigla deve ter no máximo 20 caracteres.") String sigla,
    @Size(max = 160, message = "A instituição deve ter no máximo 160 caracteres.")
        String instituicao,
    String cnpj,
    @NotBlank(message = "O fuso horário é obrigatório.")
        @Size(max = 60, message = "O fuso horário deve ter no máximo 60 caracteres.")
        String fusoHorario,
    @NotNull(message = "A situação da atlética é obrigatória.") Boolean ativa,
    @NotNull(message = "A versão é obrigatória.")
        @PositiveOrZero(message = "A versão deve ser maior ou igual a zero.")
        Long version)
    implements UpdateRequest {}
