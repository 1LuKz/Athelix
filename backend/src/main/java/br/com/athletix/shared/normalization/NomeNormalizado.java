package br.com.athletix.shared.normalization;

import br.com.athletix.shared.exception.BusinessRuleException;

import java.util.Locale;

/** Chave de unicidade que ignora caixa e espaços nas extremidades sem alterar o nome exibido. */
public final class NomeNormalizado {

    private NomeNormalizado() {
    }

    public static String fromRequired(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleException("O campo " + field + " é obrigatório.");
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    public static String displayValue(String value) {
        return value.trim();
    }

    public static String optionalDisplayValue(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
