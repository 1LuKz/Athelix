package br.com.athletix.auth.service;

/** Falha de infraestrutura SMTP; nunca contém o token ou o link secreto. */
public class CredentialDeliveryException extends RuntimeException {
  public CredentialDeliveryException(Throwable cause) {
    super("Não foi possível entregar a mensagem de credencial.", cause);
  }
}
