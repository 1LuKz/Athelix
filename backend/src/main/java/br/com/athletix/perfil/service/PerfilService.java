package br.com.athletix.perfil.service;

import br.com.athletix.auditoria.service.AtividadeUsuarioService;
import br.com.athletix.auth.service.InvalidacaoSessaoUsuarioService;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.membro.repository.MembroRepository;
import br.com.athletix.perfil.dto.PerfilSenhaUpdateRequest;
import br.com.athletix.perfil.dto.PerfilUpdateRequest;
import br.com.athletix.shared.exception.BusinessRuleException;
import br.com.athletix.shared.exception.ConcurrentModificationException;
import br.com.athletix.shared.normalization.NomeNormalizado;
import br.com.athletix.shared.tenancy.AtleticaResourceResolver;
import br.com.athletix.usuario.entity.Usuario;
import br.com.athletix.usuario.repository.UsuarioRepository;
import br.com.athletix.usuario.service.PasswordPolicy;
import br.com.athletix.usuario.service.TelefoneNormalizado;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Atualizações que o próprio membro pode fazer sem alterar cargo, departamento ou e-mail. */
@Service
public class PerfilService {
  private final UsuarioRepository usuarioRepository;
  private final MembroRepository membroRepository;
  private final AtleticaResourceResolver resourceResolver;
  private final PasswordEncoder passwordEncoder;
  private final InvalidacaoSessaoUsuarioService invalidacaoSessaoUsuarioService;
  private final AtividadeUsuarioService atividadeService;

  public PerfilService(
      UsuarioRepository usuarioRepository,
      MembroRepository membroRepository,
      AtleticaResourceResolver resourceResolver,
      PasswordEncoder passwordEncoder,
      InvalidacaoSessaoUsuarioService invalidacaoSessaoUsuarioService,
      AtividadeUsuarioService atividadeService) {
    this.usuarioRepository = usuarioRepository;
    this.membroRepository = membroRepository;
    this.resourceResolver = resourceResolver;
    this.passwordEncoder = passwordEncoder;
    this.invalidacaoSessaoUsuarioService = invalidacaoSessaoUsuarioService;
    this.atividadeService = atividadeService;
  }

  @Transactional(readOnly = true)
  public PerfilAtual carregar(Long membroId) {
    Membro membro = membroAtual(membroId);
    Usuario usuario = usuarioRepository.findById(membro.getUsuarioId()).orElseThrow();
    return new PerfilAtual(usuario, membro);
  }

  @Transactional
  public PerfilAtual atualizar(Long membroId, PerfilUpdateRequest request) {
    Membro membro = membroAtual(membroId);
    Usuario usuario = usuarioRepository.findById(membro.getUsuarioId()).orElseThrow();
    if (!usuario.getVersion().equals(request.usuarioVersion())
        || !membro.getVersion().equals(request.membroVersion())) {
      throw new ConcurrentModificationException();
    }
    String nome = NomeNormalizado.displayValue(request.nome());
    String curso = NomeNormalizado.displayValue(request.curso());
    usuario.atualizarDados(nome, TelefoneNormalizado.normalize(request.telefone()));
    membro.atualizarVinculo(
        curso, request.periodoCurso(), membro.getCargo(), membro.getDepartamento());
    atividadeService.registrar(
        "PERFIL_ATUALIZADO", "PERFIL", membro.getId(), "Dados de perfil atualizados.");
    return new PerfilAtual(usuario, membro);
  }

  @Transactional
  public void alterarSenha(Long membroId, PerfilSenhaUpdateRequest request, String sessionId) {
    Membro membro = membroAtual(membroId);
    Usuario usuario = usuarioRepository.findById(membro.getUsuarioId()).orElseThrow();
    if (!usuario.getVersion().equals(request.version())) {
      throw new ConcurrentModificationException();
    }
    if (!usuario.possuiCredencialDefinida()
        || !passwordEncoder.matches(request.senhaAtual(), usuario.senhaHashParaAutenticacao())) {
      throw new BusinessRuleException("A senha atual é inválida.");
    }
    PasswordPolicy.validate(request.novaSenha());
    usuario.definirSenhaHash(passwordEncoder.encode(request.novaSenha()));
    invalidacaoSessaoUsuarioService.invalidarSessoesExceto(usuario.getId(), sessionId);
    atividadeService.registrar(
        "SENHA_ALTERADA", "PERFIL", membro.getId(), "Senha do perfil alterada.");
  }

  private Membro membroAtual(Long membroId) {
    return resourceResolver.requireByIdAndAtleticaId(
        membroId, membroRepository::findByIdAndAtletica_Id);
  }

  public record PerfilAtual(Usuario usuario, Membro membro) {}
}
