package br.com.athletix.patrimonio.emprestimo.dto;

import br.com.athletix.patrimonio.entity.ConservacaoPatrimonio;
import jakarta.validation.constraints.*;
import java.time.Instant;

public record DevolucaoPatrimonioCreateRequest(
    @Positive int quantidade,
    Instant devolvidaEm,
    @NotNull ConservacaoPatrimonio condicaoRetorno,
    @Size(max = 1000) String observacao,
    boolean enviarParaManutencao,
    @Size(max = 1000) String motivoManutencao,
    Instant previsaoConclusaoManutencao) {}
