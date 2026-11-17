package br.com.athletix.membro.dto;

import br.com.athletix.membro.entity.SolicitacaoAlteracaoVinculo;
import br.com.athletix.membro.entity.StatusSolicitacaoAlteracaoVinculo;
import br.com.athletix.shared.api.dto.ResponseDto;
import java.time.Instant;

public record SolicitacaoAlteracaoVinculoResponse(
    Long id,
    Long membroId,
    Long cargoPretendidoId,
    String cargoPretendido,
    Long departamentoPretendidoId,
    String departamentoPretendido,
    Long cargoAnteriorId,
    Long departamentoAnteriorId,
    String motivo,
    StatusSolicitacaoAlteracaoVinculo status,
    Instant avaliadaEm,
    Long avaliadaPorMembroId,
    String observacaoAvaliacao,
    Long version,
    Instant createdAt,
    Instant updatedAt)
    implements ResponseDto {
  public static SolicitacaoAlteracaoVinculoResponse from(SolicitacaoAlteracaoVinculo solicitacao) {
    return new SolicitacaoAlteracaoVinculoResponse(
        solicitacao.getId(),
        solicitacao.getMembroId(),
        solicitacao.getCargoPretendidoId(),
        solicitacao.getNomeCargoPretendido(),
        solicitacao.getDepartamentoPretendidoId(),
        solicitacao.getNomeDepartamentoPretendido(),
        solicitacao.getCargoAnteriorId(),
        solicitacao.getDepartamentoAnteriorId(),
        solicitacao.getMotivo(),
        solicitacao.getStatus(),
        solicitacao.getAvaliadaEm(),
        solicitacao.getAvaliadaPorMembroId(),
        solicitacao.getObservacaoAvaliacao(),
        solicitacao.getVersion(),
        solicitacao.getCreatedAt(),
        solicitacao.getUpdatedAt());
  }
}
