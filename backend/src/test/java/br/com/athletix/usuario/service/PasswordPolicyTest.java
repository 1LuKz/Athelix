package br.com.athletix.usuario.service;

import br.com.athletix.shared.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PasswordPolicyTest {

    @Test
    void acceptsOnlyPasswordsWithinCharacterAndBcryptByteLimits() {
        assertDoesNotThrow(() -> PasswordPolicy.validate("SenhaSegura1"));
        assertThrows(BusinessRuleException.class, () -> PasswordPolicy.validate("curta123"));
        assertThrows(BusinessRuleException.class, () -> PasswordPolicy.validate("á".repeat(40)));
    }
}
