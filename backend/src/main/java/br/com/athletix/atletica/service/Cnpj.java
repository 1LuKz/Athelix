package br.com.athletix.atletica.service;

import br.com.athletix.shared.exception.BusinessRuleException;

/** Normaliza e valida CNPJ quando ele é informado; a posse de CNPJ não é obrigatória. */
public final class Cnpj {

  private static final int[] FIRST_DIGIT_WEIGHTS = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
  private static final int[] SECOND_DIGIT_WEIGHTS = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

  private Cnpj() {}

  public static String normalize(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    String digits = value.replaceAll("\\D", "");
    if (digits.length() != 14
        || allDigitsEqual(digits)
        || calculateDigit(digits, FIRST_DIGIT_WEIGHTS) != digitAt(digits, 12)
        || calculateDigit(digits, SECOND_DIGIT_WEIGHTS) != digitAt(digits, 13)) {
      throw new BusinessRuleException("O CNPJ informado é inválido.");
    }
    return digits;
  }

  private static boolean allDigitsEqual(String digits) {
    return digits.chars().distinct().count() == 1;
  }

  private static int calculateDigit(String cnpj, int[] weights) {
    int sum = 0;
    for (int index = 0; index < weights.length; index++) {
      sum += digitAt(cnpj, index) * weights[index];
    }
    int remainder = sum % 11;
    return remainder < 2 ? 0 : 11 - remainder;
  }

  private static int digitAt(String value, int index) {
    return Character.digit(value.charAt(index), 10);
  }
}
