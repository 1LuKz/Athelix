package br.com.athletix.auth.dto;

import br.com.athletix.cargo.entity.PerfilAcesso;
import br.com.athletix.shared.api.dto.ResponseDto;

public record VinculoAtleticaResponse(
    Long membroId,
    Long atleticaId,
    String atleticaNome,
    String fusoHorario,
    Long departamentoId,
    PerfilAcesso perfilAcesso)
    implements ResponseDto {}
