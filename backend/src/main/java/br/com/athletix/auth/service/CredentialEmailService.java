package br.com.athletix.auth.service;

import br.com.athletix.auth.config.AthletixMailProperties;
import br.com.athletix.auth.entity.FinalidadeTokenCredencial;
import br.com.athletix.usuario.entity.Usuario;
import java.nio.charset.StandardCharsets;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

/** Envio SMTP real; o segredo só existe nesta chamada para compor o link. */
@Service
public class CredentialEmailService {
  private final JavaMailSender mailSender;
  private final AthletixMailProperties properties;

  public CredentialEmailService(JavaMailSender mailSender, AthletixMailProperties properties) {
    this.mailSender = mailSender;
    this.properties = properties;
  }

  public void enviar(Usuario usuario, FinalidadeTokenCredencial finalidade, String tokenOriginal) {
    String rota =
        finalidade == FinalidadeTokenCredencial.RECUPERAR_SENHA
            ? "/redefinir-senha"
            : "/definir-senha";
    String link =
        properties.getFrontendBaseUrl().replaceFirst("/+$", "")
            + rota
            + "?token="
            + UriUtils.encodeQueryParam(tokenOriginal, StandardCharsets.UTF_8);
    SimpleMailMessage mensagem = new SimpleMailMessage();
    mensagem.setFrom(properties.getMailFrom());
    mensagem.setTo(usuario.getEmail());
    mensagem.setSubject(
        finalidade == FinalidadeTokenCredencial.RECUPERAR_SENHA
            ? "Athletix — redefinição de senha"
            : "Athletix — defina sua senha");
    mensagem.setText(
        "Olá, "
            + usuario.getNome()
            + ",\n\nUse o link abaixo para continuar:\n"
            + link
            + "\n\nSe você não solicitou esta ação, ignore esta mensagem.");
    try {
      mailSender.send(mensagem);
    } catch (MailException exception) {
      throw new CredentialDeliveryException(exception);
    }
  }
}
