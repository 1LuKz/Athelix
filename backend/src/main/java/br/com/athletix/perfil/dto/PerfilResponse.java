package br.com.athletix.perfil.dto;

import br.com.athletix.membro.entity.Membro;
import br.com.athletix.shared.api.dto.ResponseDto;
import br.com.athletix.usuario.entity.Usuario;

public record PerfilResponse(
    Long usuarioId,
    String nome,
    String email,
    String telefone,
    Long membroId,
    String curso,
    Integer periodoCurso,
    Long cargoId,
    String cargo,
    Long departamentoId,
    String departamento,
    Long usuarioVersion,
    Long membroVersion)
    implements ResponseDto {
  public static PerfilResponse from(Usuario usuario, Membro membro) {
    return new PerfilResponse(
        usuario.getId(),
        usuario.getNome(),
        usuario.getEmail(),
        usuario.getTelefone(),
        membro.getId(),
        membro.getCurso(),
        membro.getPeriodoCurso(),
        membro.getCargoId(),
        membro.getNomeCargo(),
        membro.getDepartamentoId(),
        membro.getNomeDepartamento(),
        usuario.getVersion(),
        membro.getVersion());
  }
}
