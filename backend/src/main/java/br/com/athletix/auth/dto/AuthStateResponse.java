package br.com.athletix.auth.dto;

import br.com.athletix.auth.service.Capacidade;
import br.com.athletix.shared.api.dto.ResponseDto;
import br.com.athletix.usuario.dto.UsuarioResponse;
import java.util.List;

/**
 * Estado seguro da sessão: não contém hash, senha, token de sessão ou dados de vínculos inativos.
 */
public record AuthStateResponse(
    UsuarioResponse usuario,
    List<VinculoAtleticaResponse> vinculosAtivos,
    Long membroSelecionadoId,
    boolean selecaoNecessaria,
    List<Capacidade> capacidades)
    implements ResponseDto {}
