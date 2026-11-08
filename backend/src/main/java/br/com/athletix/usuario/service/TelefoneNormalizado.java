package br.com.athletix.usuario.service;

import br.com.athletix.shared.exception.BusinessRuleException;

/** Telefones são persistidos no formato internacional +<dígitos>. */
public final class TelefoneNormalizado {

    private TelefoneNormalizado() {
    }

    public static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String digits = value.replaceAll("\\D", "");
        if (digits.length() < 8 || digits.length() > 19) {
            throw new BusinessRuleException("O telefone informado é inválido.");
        }
        return "+" + digits;
    }
}
