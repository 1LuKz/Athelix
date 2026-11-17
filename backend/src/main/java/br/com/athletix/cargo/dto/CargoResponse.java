package br.com.athletix.cargo.dto;

import br.com.athletix.cargo.entity.Cargo;
import br.com.athletix.cargo.entity.PerfilAcesso;
import br.com.athletix.shared.api.dto.ResponseDto;
import java.time.Instant;

public record CargoResponse(
    Long id,
    Long atleticaId,
    String nome,
    String descricao,
    PerfilAcesso perfilAcesso,
    boolean ativo,
    Long version,
    Instant createdAt,
    Instant updatedAt)
    implements ResponseDto {

  public static CargoResponse from(Cargo cargo) {
    return new CargoResponse(
        cargo.getId(),
        cargo.getAtleticaId(),
        cargo.getNome(),
        cargo.getDescricao(),
        cargo.getPerfilAcesso(),
        cargo.isAtivo(),
        cargo.getVersion(),
        cargo.getCreatedAt(),
        cargo.getUpdatedAt());
  }
}
