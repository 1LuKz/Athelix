package br.com.athletix.atletica.dto;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.shared.api.dto.ResponseDto;
import java.time.Instant;

public record AtleticaResponse(
    Long id,
    String nome,
    String sigla,
    String instituicao,
    String cnpj,
    String fusoHorario,
    boolean ativa,
    Long version,
    Instant createdAt,
    Instant updatedAt)
    implements ResponseDto {

  public static AtleticaResponse from(Atletica atletica) {
    return new AtleticaResponse(
        atletica.getId(),
        atletica.getNome(),
        atletica.getSigla(),
        atletica.getInstituicao(),
        atletica.getCnpj(),
        atletica.getFusoHorario(),
        atletica.isAtiva(),
        atletica.getVersion(),
        atletica.getCreatedAt(),
        atletica.getUpdatedAt());
  }
}
