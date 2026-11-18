package br.com.athletix.auth.service;

import br.com.athletix.auth.dto.AuthStateResponse;
import br.com.athletix.auth.dto.VinculoAtleticaResponse;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.membro.entity.StatusMembro;
import br.com.athletix.membro.repository.MembroRepository;
import br.com.athletix.shared.exception.ResourceNotFoundException;
import br.com.athletix.usuario.dto.UsuarioResponse;
import br.com.athletix.usuario.entity.Usuario;
import br.com.athletix.usuario.repository.UsuarioRepository;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SessaoAtleticaService {
  public static final String MEMBRO_SELECIONADO_SESSION_ATTRIBUTE = "ATHLETIX_MEMBRO_SELECIONADO";

  private final UsuarioRepository usuarioRepository;
  private final MembroRepository membroRepository;
  private final CapacidadeService capacidadeService;

  public SessaoAtleticaService(
      UsuarioRepository usuarioRepository,
      MembroRepository membroRepository,
      CapacidadeService capacidadeService) {
    this.usuarioRepository = usuarioRepository;
    this.membroRepository = membroRepository;
    this.capacidadeService = capacidadeService;
  }

  @Transactional(readOnly = true)
  public AuthStateResponse estado(Long usuarioId, Long membroSelecionadoId) {
    Usuario usuario =
        usuarioRepository.findById(usuarioId).orElseThrow(ResourceNotFoundException::new);
    List<Membro> membros = vinculosElegiveis(usuarioId);
    Long selecionado =
        membros.stream()
            .map(Membro::getId)
            .filter(identificadorMembro -> identificadorMembro.equals(membroSelecionadoId))
            .findFirst()
            .orElse(null);
    if (selecionado == null && membros.size() == 1) {
      selecionado = membros.get(0).getId();
    }
    Long idSelecionadoAtual = selecionado;
    Membro membroSelecionado =
        idSelecionadoAtual == null
            ? null
            : membros.stream()
                .filter(membro -> membro.getId().equals(idSelecionadoAtual))
                .findFirst()
                .orElseThrow();
    return new AuthStateResponse(
        UsuarioResponse.from(usuario),
        membros.stream().map(this::vinculo).toList(),
        selecionado,
        membros.size() > 1 && selecionado == null,
        membroSelecionado == null
            ? List.of()
            : capacidadeService.para(membroSelecionado.getPerfilAcesso()));
  }

  @Transactional(readOnly = true)
  public Membro resolverSelecionado(Long usuarioId, Long membroId) {
    Usuario usuario =
        usuarioRepository
            .findById(usuarioId)
            .orElseThrow(() -> new AccessDeniedException("Credencial inválida."));
    if (!usuario.isCredencialAtiva() || !usuario.possuiCredencialDefinida()) {
      throw new AccessDeniedException("Credencial indisponível.");
    }
    Membro membro =
        membroRepository
            .findByIdAndUsuario_Id(membroId, usuarioId)
            .orElseThrow(() -> new AccessDeniedException("Vínculo inválido."));
    if (!elegivel(membro)) {
      throw new AccessDeniedException("O vínculo selecionado não está disponível.");
    }
    return membro;
  }

  @Transactional(readOnly = true)
  public Long selecionar(Long usuarioId, Long membroId) {
    return resolverSelecionado(usuarioId, membroId).getId();
  }

  private List<Membro> vinculosElegiveis(Long usuarioId) {
    return membroRepository
        .findAllByUsuario_IdAndStatusOrderByAtletica_NomeAsc(usuarioId, StatusMembro.ATIVO)
        .stream()
        .filter(this::elegivel)
        .toList();
  }

  private boolean elegivel(Membro membro) {
    return membro.atleticaAtiva() && membro.cargoAtivo();
  }

  private VinculoAtleticaResponse vinculo(Membro membro) {
    return new VinculoAtleticaResponse(
        membro.getId(),
        membro.getAtleticaId(),
        membro.getNomeAtletica(),
        membro.getFusoHorarioAtletica(),
        membro.getDepartamentoId(),
        membro.getPerfilAcesso());
  }
}
