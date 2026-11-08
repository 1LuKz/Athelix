package br.com.athletix.atletica.service;

import br.com.athletix.shared.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DominioEmailTest {

    @Test
    void normalizesTheEntireDomain() {
        assertEquals("engenharia.exemplo.edu.br", DominioEmail.normalize(" Engenharia.Exemplo.EDU.br "));
        assertNotEquals(
                DominioEmail.normalize("engenharia.exemplo.edu.br"),
                DominioEmail.normalize("outroengenharia.exemplo.edu.br")
        );
    }

    @Test
    void rejectsEmailAddressesAndPartialDomains() {
        assertThrows(BusinessRuleException.class, () -> DominioEmail.normalize("contato@exemplo.edu.br"));
        assertThrows(BusinessRuleException.class, () -> DominioEmail.normalize("localhost"));
    }
}
