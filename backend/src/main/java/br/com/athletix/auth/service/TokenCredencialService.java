package br.com.athletix.auth.service;

import br.com.athletix.auth.entity.FinalidadeTokenCredencial;
import br.com.athletix.auth.entity.TokenCredencial;
import br.com.athletix.auth.repository.TokenCredencialRepository;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.membro.repository.MembroRepository;
import br.com.athletix.shared.exception.BusinessRuleException;
import br.com.athletix.shared.exception.ResourceNotFoundException;
import br.com.athletix.usuario.dto.SenhaDefinicaoRequest;
import br.com.athletix.usuario.entity.Usuario;
import br.com.athletix.usuario.repository.UsuarioRepository;
import br.com.athletix.usuario.service.EmailNormalizado;
import br.com.athletix.usuario.service.UsuarioService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TokenCredencialService {
  private static final Duration VALIDADE_RECUPERACAO = Duration.ofMinutes(30);
  private static final Duration VALIDADE_CONVITE = Duration.ofHours(24);
  private static final SecureRandom RANDOM = new SecureRandom();

  private final TokenCredencialRepository tokenRepository;
  private final UsuarioRepository usuarioRepository;
  private final MembroRepository membroRepository;
  private final UsuarioService usuarioService;
  private final CredentialEmailService emailService;
  private final InvalidacaoSessaoUsuarioService invalidacaoSessaoUsuarioService;

  public TokenCredencialService(
      TokenCredencialRepository tokenRepository,
      UsuarioRepository usuarioRepository,
      MembroRepository membroRepository,
      UsuarioService usuarioService,
      CredentialEmailService emailService,
      InvalidacaoSessaoUsuarioService invalidacaoSessaoUsuarioService) {
    this.tokenRepository = tokenRepository;
    this.usuarioRepository = usuarioRepository;
    this.membroRepository = membroRepository;
    this.usuarioService = usuarioService;
    this.emailService = emailService;
    this.invalidacaoSessaoUsuarioService = invalidacaoSessaoUsuarioService;
  }

  /** Não revela a existência da conta: entradas inválidas e desconhecidas não geram exceção. */
  @Transactional
  public void solicitarRecuperacao(String emailInformado) {
    Optional<Usuario> usuario = buscarUsuarioPorEmailSeguro(emailInformado);
    if (usuario.isEmpty()
        || !usuario.get().isCredencialAtiva()
        || !usuario.get().possuiCredencialDefinida()) {
      return;
    }
    emitir(usuario.get(), null, FinalidadeTokenCredencial.RECUPERAR_SENHA);
  }

  /**
   * Preparado para o fluxo administrativo de convite; uma credencial existente nunca é alterada.
   */
  @Transactional
  public boolean emitirConviteParaMembro(Long membroId) {
    Membro membro = membroRepository.findById(membroId).orElseThrow(ResourceNotFoundException::new);
    Usuario usuario =
        usuarioRepository
            .findById(membro.getUsuarioId())
            .orElseThrow(ResourceNotFoundException::new);
    if (usuario.possuiCredencialDefinida()) {
      return false;
    }
    emitir(usuario, membro, FinalidadeTokenCredencial.DEFINIR_SENHA);
    return true;
  }

  @Transactional
  public void redefinirSenha(String tokenOriginal, String novaSenha) {
    TokenCredencial token = consumir(tokenOriginal, FinalidadeTokenCredencial.RECUPERAR_SENHA);
    usuarioService.definirSenha(token.getUsuarioId(), new SenhaDefinicaoRequest(novaSenha));
    invalidacaoSessaoUsuarioService.invalidarSessoesAnteriores(token.getUsuarioId());
  }

  @Transactional
  public void definirSenha(String tokenOriginal, String novaSenha) {
    TokenCredencial token = consumir(tokenOriginal, FinalidadeTokenCredencial.DEFINIR_SENHA);
    usuarioService.definirSenha(token.getUsuarioId(), new SenhaDefinicaoRequest(novaSenha));
  }

  private void emitir(Usuario usuario, Membro membro, FinalidadeTokenCredencial finalidade) {
    String tokenOriginal = novoSegredo();
    TokenCredencial token =
        new TokenCredencial(
            usuario,
            membro,
            finalidade,
            resumo(tokenOriginal),
            Instant.now()
                .plus(
                    finalidade == FinalidadeTokenCredencial.RECUPERAR_SENHA
                        ? VALIDADE_RECUPERACAO
                        : VALIDADE_CONVITE));
    tokenRepository.save(token);
    // Se o SMTP falhar, a exceção provoca rollback e nenhum token utilizável é mantido.
    emailService.enviar(usuario, finalidade, tokenOriginal);
  }

  private TokenCredencial consumir(String tokenOriginal, FinalidadeTokenCredencial finalidade) {
    if (tokenOriginal == null || tokenOriginal.isBlank()) {
      throw tokenInvalido();
    }
    TokenCredencial token =
        tokenRepository
            .findByTokenHashForUpdate(resumo(tokenOriginal))
            .orElseThrow(this::tokenInvalido);
    Instant agora = Instant.now();
    if (token.getFinalidade() != finalidade || !token.estaDisponivelEm(agora)) {
      throw tokenInvalido();
    }
    token.consumir(agora);
    tokenRepository.consumirOutrosDisponiveis(
        token.getUsuarioId(), finalidade, token.getId(), agora);
    return token;
  }

  private Optional<Usuario> buscarUsuarioPorEmailSeguro(String emailInformado) {
    try {
      return usuarioRepository.findByEmail(EmailNormalizado.normalize(emailInformado));
    } catch (BusinessRuleException exception) {
      return Optional.empty();
    }
  }

  private String novoSegredo() {
    byte[] bytes = new byte[32];
    RANDOM.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private String resumo(String token) {
    try {
      byte[] digest =
          MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
      return java.util.HexFormat.of().formatHex(digest);
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 não está disponível.", exception);
    }
  }

  private BusinessRuleException tokenInvalido() {
    return new BusinessRuleException("O token é inválido, expirou ou já foi utilizado.");
  }
}
