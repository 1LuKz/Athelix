package br.com.athletix.departamento.dto;

import br.com.athletix.departamento.entity.Departamento;
import br.com.athletix.departamento.entity.SituacaoDepartamento;
import br.com.athletix.shared.api.dto.ResponseDto;
import java.time.Instant;

/** A contagem de membros não é persistida aqui; B05 a calculará pelos vínculos. */
public record DepartamentoResponse(
    Long id,
    Long atleticaId,
    String nome,
    String descricao,
    Long responsavelMembroId,
    String responsavelNome,
    SituacaoDepartamento situacao,
    long quantidadeMembros,
    Long version,
    Instant createdAt,
    Instant updatedAt)
    implements ResponseDto {

  public static DepartamentoResponse from(Departamento departamento) {
    return from(departamento, null, 0);
  }

  public static DepartamentoResponse from(
      Departamento departamento, String responsavelNome, long quantidadeMembros) {
    return new DepartamentoResponse(
        departamento.getId(),
        departamento.getAtleticaId(),
        departamento.getNome(),
        departamento.getDescricao(),
        departamento.getResponsavelMembroId(),
        responsavelNome,
        departamento.getSituacao(),
        quantidadeMembros,
        departamento.getVersion(),
        departamento.getCreatedAt(),
        departamento.getUpdatedAt());
  }
}
