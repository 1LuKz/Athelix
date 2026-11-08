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
        SituacaoDepartamento situacao,
        Long version,
        Instant createdAt,
        Instant updatedAt
) implements ResponseDto {

    public static DepartamentoResponse from(Departamento departamento) {
        return new DepartamentoResponse(
                departamento.getId(), departamento.getAtleticaId(), departamento.getNome(),
                departamento.getDescricao(), departamento.getResponsavelMembroId(), departamento.getSituacao(),
                departamento.getVersion(), departamento.getCreatedAt(), departamento.getUpdatedAt()
        );
    }
}
