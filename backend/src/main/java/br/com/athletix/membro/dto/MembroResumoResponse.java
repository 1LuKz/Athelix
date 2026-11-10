package br.com.athletix.membro.dto;

import br.com.athletix.membro.entity.Membro;
import br.com.athletix.membro.entity.StatusMembro;
import br.com.athletix.shared.api.dto.ResponseDto;

import java.time.Instant;
import java.time.LocalDate;

/** Visão de diretório; e-mail e telefone só são preenchidos quando o perfil atual pode consultá-los. */
public record MembroResumoResponse(
        Long id,
        Long usuarioId,
        String nome,
        String email,
        String telefone,
        String curso,
        Integer periodoCurso,
        Long cargoId,
        String cargo,
        Long departamentoId,
        String departamento,
        StatusMembro status,
        LocalDate membroDesde,
        Instant ativadoEm,
        Instant inativadoEm,
        Long version
) implements ResponseDto {
    public static MembroResumoResponse from(Membro membro, boolean incluirContato) {
        return new MembroResumoResponse(
                membro.getId(), membro.getUsuarioId(), membro.getNomeUsuario(),
                incluirContato ? membro.getEmailUsuario() : null,
                incluirContato ? membro.getTelefoneUsuario() : null,
                membro.getCurso(), membro.getPeriodoCurso(), membro.getCargoId(), membro.getNomeCargo(),
                membro.getDepartamentoId(), membro.getNomeDepartamento(), membro.getStatus(), membro.getMembroDesde(),
                membro.getAtivadoEm(), membro.getInativadoEm(), membro.getVersion()
        );
    }
}
