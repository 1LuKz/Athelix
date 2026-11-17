package br.com.athletix.usuario.service;

import br.com.athletix.shared.exception.BusinessRuleException;
import java.nio.charset.StandardCharsets;

/** Política única para definição e recuperação de senha; BCrypt nunca recebe bytes truncados. */
public final class PasswordPolicy {
  public static final int MIN_LENGTH = 10;
  public static final int MAX_LENGTH = 64;
  public static final int BCRYPT_MAX_BYTES = 72;

  private PasswordPolicy() {}

  public static void validate(String password) {
    if (password == null || password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
      throw new BusinessRuleException("A senha deve ter entre 10 e 64 caracteres.");
    }
    if (password.getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_BYTES) {
      throw new BusinessRuleException("A senha excede o limite de bytes aceito pelo BCrypt.");
    }
  }
}
