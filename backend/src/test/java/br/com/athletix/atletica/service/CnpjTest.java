package br.com.athletix.atletica.service;

import br.com.athletix.shared.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CnpjTest {

    @Test
    void normalizesAValidFormattedCnpj() {
        assertEquals("11222333000181", Cnpj.normalize("11.222.333/0001-81"));
    }

    @Test
    void acceptsTheAbsenceOfCnpj() {
        assertNull(Cnpj.normalize("  "));
    }

    @Test
    void rejectsInvalidCnpj() {
        assertThrows(BusinessRuleException.class, () -> Cnpj.normalize("11.222.333/0001-80"));
    }
}
