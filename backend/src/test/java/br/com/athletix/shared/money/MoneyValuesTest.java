package br.com.athletix.shared.money;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.athletix.shared.exception.BusinessRuleException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MoneyValuesTest {

  @Test
  void normalizesWithConfiguredHalfUpRounding() {
    BigDecimal value = MoneyValues.parse("1250.905");

    assertEquals(new BigDecimal("1250.91"), value);
    assertEquals("1250.91", MoneyValues.format(value));
  }

  @Test
  void rejectsNonDecimalTransportValues() {
    assertThrows(BusinessRuleException.class, () -> MoneyValues.parse("1,250.90"));
  }
}
