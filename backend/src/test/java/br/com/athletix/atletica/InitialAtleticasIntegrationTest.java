package br.com.athletix.atletica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.athletix.atletica.repository.AtleticaRepository;
import br.com.athletix.atletica.repository.DominioEmailPermitidoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("dev")
class InitialAtleticasIntegrationTest {

  @Autowired private AtleticaRepository atleticaRepository;

  @Autowired private DominioEmailPermitidoRepository dominioRepository;

  @Test
  void loadsInitialAtleticasWithoutRequiringCnpj() {
    assertEquals(2, atleticaRepository.count());
    assertTrue(
        atleticaRepository.findAll().stream().allMatch(atletica -> atletica.getCnpj() == null));
    assertEquals(2, dominioRepository.count());
  }

  @Test
  void comparesEmailDomainsByExactValueInsideTheAtleticaScope() {
    Long atleticaId = atleticaRepository.findAll().get(0).getId();

    assertTrue(
        dominioRepository.existsByAtletica_IdAndDominio(atleticaId, "engenharia.exemplo.edu.br")
            || dominioRepository.existsByAtletica_IdAndDominio(atleticaId, "saude.exemplo.edu.br"));
    assertFalse(
        dominioRepository.existsByAtletica_IdAndDominio(
            atleticaId, "outroengenharia.exemplo.edu.br"));
  }
}
