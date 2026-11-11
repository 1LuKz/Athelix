package br.com.athletix.membro.dto;

import br.com.athletix.membro.entity.DecisaoSolicitacaoAlteracaoVinculo;
import br.com.athletix.shared.api.dto.UpdateRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record SolicitacaoAlteracaoVinculoDecisaoRequest(
        @NotNull DecisaoSolicitacaoAlteracaoVinculo decisao,
        @Size(max = 500) String observacao,
        @NotNull @PositiveOrZero Long version,
        @NotNull @PositiveOrZero Long membroVersion
) implements UpdateRequest { }
