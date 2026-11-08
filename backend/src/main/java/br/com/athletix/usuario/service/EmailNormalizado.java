package br.com.athletix.usuario.service;

import br.com.athletix.shared.exception.BusinessRuleException;

import java.net.IDN;
import java.util.Locale;

/** Normalização conservadora para a chave global de identidade. */
public final class EmailNormalizado {

    private static final int MAX_LENGTH = 254;
    private static final String LOCAL_PART = "[a-z0-9.!#$%&'*+/=?^_`{|}~-]+";
    private static final String DOMAIN = "(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?";

    private EmailNormalizado() {
    }

    public static String normalize(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleException("O e-mail é obrigatório.");
        }

        String trimmed = value.trim();
        int separator = trimmed.lastIndexOf('@');
        if (separator <= 0 || separator != trimmed.indexOf('@') || separator == trimmed.length() - 1) {
            throw new BusinessRuleException("O e-mail informado é inválido.");
        }

        try {
            String localPart = trimmed.substring(0, separator).toLowerCase(Locale.ROOT);
            String domain = IDN.toASCII(trimmed.substring(separator + 1).toLowerCase(Locale.ROOT), IDN.USE_STD3_ASCII_RULES);
            String normalized = localPart + "@" + domain;
            if (normalized.length() > MAX_LENGTH || !localPart.matches(LOCAL_PART) || !domain.matches(DOMAIN)) {
                throw new BusinessRuleException("O e-mail informado é inválido.");
            }
            return normalized;
        } catch (IllegalArgumentException exception) {
            throw new BusinessRuleException("O e-mail informado é inválido.");
        }
    }
}
