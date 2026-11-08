package br.com.athletix.membro.dto;

import br.com.athletix.membro.entity.Membro;
import br.com.athletix.membro.entity.StatusMembro;
import br.com.athletix.shared.api.dto.ResponseDto;

import java.time.Instant;
import java.time.LocalDate;

public record MembroResponse(
        Long id,
        Long atleticaId,
        Long usuarioId,
        String curso,
        Integer periodoCurso,
        Long cargoId,
        Long departamentoId,
        StatusMembro status,
        LocalDate membroDesde,
        Instant ativadoEm,
        Instant inativadoEm,
        Long validadoPorMembroId,
        Long version,
        Instant createdAt,
        Instant updatedAt
) implements ResponseDto {

    public static MembroResponse from(Membro membro) {
        return new MembroResponse(
                membro.getId(), membro.getAtleticaId(), membro.getUsuarioId(), membro.getCurso(),
                membro.getPeriodoCurso(), membro.getCargoId(), membro.getDepartamentoId(), membro.getStatus(),
                membro.getMembroDesde(), membro.getAtivadoEm(), membro.getInativadoEm(), membro.getValidadoPorMembroId(),
                membro.getVersion(), membro.getCreatedAt(), membro.getUpdatedAt()
        );
    }
}
