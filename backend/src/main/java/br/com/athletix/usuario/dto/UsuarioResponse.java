package br.com.athletix.usuario.dto;

import br.com.athletix.shared.api.dto.ResponseDto;
import br.com.athletix.usuario.entity.Usuario;

import java.time.Instant;

/** A senha, seu hash e tokens não fazem parte deste contrato. */
public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        String telefone,
        boolean credencialAtiva,
        Instant ultimoAcessoEm,
        Instant senhaAlteradaEm,
        Long version,
        Instant createdAt,
        Instant updatedAt
) implements ResponseDto {

    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getTelefone(),
                usuario.isCredencialAtiva(), usuario.getUltimoAcessoEm(), usuario.getSenhaAlteradaEm(),
                usuario.getVersion(), usuario.getCreatedAt(), usuario.getUpdatedAt()
        );
    }
}
