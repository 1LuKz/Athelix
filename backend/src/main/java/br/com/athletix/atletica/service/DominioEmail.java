package br.com.athletix.atletica.service;

import br.com.athletix.shared.exception.BusinessRuleException;
import java.net.IDN;
import java.util.Locale;

/** Valida o domínio inteiro; não há autorização por sufixo ou correspondência parcial. */
public final class DominioEmail {

  private static final int MAX_LENGTH = 253;
  private static final String LABEL = "[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?";
  private static final String COMPLETE_DOMAIN = "(?:" + LABEL + "\\.)+" + LABEL;

  private DominioEmail() {}

  public static String normalize(String value) {
    if (value == null || value.isBlank()) {
      throw new BusinessRuleException("O domínio de e-mail é obrigatório.");
    }

    try {
      String normalized =
          IDN.toASCII(value.trim().toLowerCase(Locale.ROOT), IDN.USE_STD3_ASCII_RULES);
      if (normalized.length() > MAX_LENGTH || !normalized.matches(COMPLETE_DOMAIN)) {
        throw new BusinessRuleException("O domínio de e-mail informado é inválido.");
      }
      return normalized;
    } catch (IllegalArgumentException exception) {
      throw new BusinessRuleException("O domínio de e-mail informado é inválido.");
    }
  }
}
