package br.com.athletix.atletica.dto;

import br.com.athletix.atletica.entity.DominioEmailPermitido;
import br.com.athletix.shared.api.dto.ResponseDto;

public record DominioEmailPermitidoResponse(Long id, Long atleticaId, String dominio)
    implements ResponseDto {

  public static DominioEmailPermitidoResponse from(DominioEmailPermitido dominio) {
    return new DominioEmailPermitidoResponse(
        dominio.getId(), dominio.getAtleticaId(), dominio.getDominio());
  }
}
