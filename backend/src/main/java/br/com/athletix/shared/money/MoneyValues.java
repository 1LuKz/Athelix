package br.com.athletix.shared.money;

import br.com.athletix.shared.exception.BusinessRuleException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Convenções de moeda persistida como DECIMAL(19,2) e exposta como texto decimal. */
public final class MoneyValues {

    public static final int PRECISION = 19;
    public static final int SCALE = 2;
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final BigDecimal MAX_VALUE = new BigDecimal("99999999999999999.99");

    private MoneyValues() {
    }

    public static BigDecimal parse(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new BusinessRuleException("O valor monetário é obrigatório.");
        }

        if (!rawValue.matches("-?\\d+(\\.\\d+)?")) {
            throw new BusinessRuleException("O valor monetário deve ser um decimal válido.");
        }

        try {
            BigDecimal normalized = new BigDecimal(rawValue).setScale(SCALE, ROUNDING_MODE);
            if (normalized.abs().compareTo(MAX_VALUE) > 0) {
                throw new BusinessRuleException("O valor monetário excede o limite permitido.");
            }
            return normalized;
        } catch (NumberFormatException exception) {
            throw new BusinessRuleException("O valor monetário deve ser um decimal válido.");
        }
    }

    public static String format(BigDecimal value) {
        return value.setScale(SCALE, ROUNDING_MODE).toPlainString();
    }
}
