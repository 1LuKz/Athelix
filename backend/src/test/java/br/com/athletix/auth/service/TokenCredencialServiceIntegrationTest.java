package br.com.athletix.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import br.com.athletix.auth.repository.TokenCredencialRepository;
import br.com.athletix.shared.exception.BusinessRuleException;
import br.com.athletix.usuario.dto.SenhaDefinicaoRequest;
import br.com.athletix.usuario.dto.UsuarioConviteCreateRequest;
import br.com.athletix.usuario.entity.Usuario;
import br.com.athletix.usuario.repository.UsuarioRepository;
import br.com.athletix.usuario.service.UsuarioService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("dev")
class TokenCredencialServiceIntegrationTest {
  private static final Pattern TOKEN = Pattern.compile("token=([^\\s]+)");

  @Autowired private TokenCredencialService tokenCredencialService;

  @Autowired private UsuarioService usuarioService;

  @Autowired private UsuarioRepository usuarioRepository;

  @Autowired private TokenCredencialRepository tokenRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @MockBean private JavaMailSender mailSender;

  @Test
  void resetsPasswordWithSingleUseTokenAndInvalidatesEarlierRecoveryTokens() {
    Usuario usuario =
        usuarioService.criarPorConvite(
            new UsuarioConviteCreateRequest(
                "Recuperação Teste", "recuperacao.teste@athletix.local", null));
    usuarioService.definirSenha(usuario.getId(), new SenhaDefinicaoRequest("SenhaInicial1"));

    tokenCredencialService.solicitarRecuperacao(usuario.getEmail());
    tokenCredencialService.solicitarRecuperacao(usuario.getEmail());
    ArgumentCaptor<SimpleMailMessage> mensagens = ArgumentCaptor.forClass(SimpleMailMessage.class);
    verify(mailSender, times(2)).send(mensagens.capture());
    String primeiroToken = tokenDa(mensagens.getAllValues().get(0));
    String segundoToken = tokenDa(mensagens.getAllValues().get(1));

    tokenCredencialService.redefinirSenha(segundoToken, "SenhaRedefinida1");
    Usuario atualizado = usuarioRepository.findById(usuario.getId()).orElseThrow();
    assertTrue(passwordEncoder.matches("SenhaRedefinida1", atualizado.senhaHashParaAutenticacao()));
    assertThrows(
        BusinessRuleException.class,
        () -> tokenCredencialService.redefinirSenha(primeiroToken, "OutraSenhaValida1"));
    assertThrows(
        BusinessRuleException.class,
        () -> tokenCredencialService.redefinirSenha(segundoToken, "OutraSenhaValida1"));
  }

  @Test
  void doesNotPersistRecoveryTokenWhenSmtpIsUnavailable() {
    Usuario usuario =
        usuarioService.criarPorConvite(
            new UsuarioConviteCreateRequest(
                "SMTP Indisponível", "smtp.indisponivel@athletix.local", null));
    usuarioService.definirSenha(usuario.getId(), new SenhaDefinicaoRequest("SenhaInicial1"));
    doThrow(new MailSendException("SMTP indisponível"))
        .when(mailSender)
        .send(any(SimpleMailMessage.class));

    assertThrows(
        CredentialDeliveryException.class,
        () -> tokenCredencialService.solicitarRecuperacao(usuario.getEmail()));

    assertThat(tokenRepository.findAll())
        .noneMatch(token -> token.getUsuarioId().equals(usuario.getId()));
  }

  private String tokenDa(SimpleMailMessage mensagem) {
    Matcher matcher = TOKEN.matcher(mensagem.getText());
    assertTrue(matcher.find());
    return matcher.group(1);
  }
}
