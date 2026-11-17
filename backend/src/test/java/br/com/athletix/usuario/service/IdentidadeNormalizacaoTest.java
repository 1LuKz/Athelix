package br.com.athletix.usuario.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.athletix.shared.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

class IdentidadeNormalizacaoTest {

  @Test
  void normalizesEmailAndPhone() {
    assertEquals(
        "maria.silva@exemplo.edu.br", EmailNormalizado.normalize(" Maria.Silva@EXEMPLO.edu.br "));
    assertEquals("+11999999999", TelefoneNormalizado.normalize("(11) 99999-9999"));
    assertNull(TelefoneNormalizado.normalize(" "));
  }

  @Test
  void rejectsInvalidIdentityValues() {
    assertThrows(BusinessRuleException.class, () -> EmailNormalizado.normalize("maria@localhost"));
    assertThrows(BusinessRuleException.class, () -> TelefoneNormalizado.normalize("123"));
  }
}
