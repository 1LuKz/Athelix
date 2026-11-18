package br.com.athletix.config.demo;

import static br.com.athletix.config.demo.DemoData.ATLETICA_ENGENHARIA;
import static br.com.athletix.config.demo.DemoData.ATLETICA_SAUDE;
import static br.com.athletix.config.demo.DemoData.ENGENHARIA_EXEMPLO_EDU_BR;
import static br.com.athletix.config.demo.DemoData.FUSO_DEMONSTRACAO;
import static br.com.athletix.config.demo.DemoData.SAUDE_EXEMPLO_EDU_BR;
import static br.com.athletix.config.demo.DemoData.SIGLA_ENGENHARIA;
import static br.com.athletix.config.demo.DemoData.SIGLA_SAUDE;
import static br.com.athletix.config.demo.DemoData.UNIVERSIDADE_EXEMPLO;

import br.com.athletix.atletica.dto.AtleticaCreateRequest;
import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.atletica.entity.DominioEmailPermitido;
import br.com.athletix.atletica.repository.AtleticaRepository;
import br.com.athletix.atletica.repository.DominioEmailPermitidoRepository;
import br.com.athletix.atletica.service.AtleticaCadastroService;
import br.com.athletix.atletica.service.DominioEmail;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
class AtleticasDemoSeeder {
  private final AtleticaRepository atleticaRepository;
  private final DominioEmailPermitidoRepository dominioRepository;
  private final AtleticaCadastroService cadastroService;

  AtleticasDemoSeeder(
      AtleticaRepository atleticaRepository,
      DominioEmailPermitidoRepository dominioRepository,
      AtleticaCadastroService cadastroService) {
    this.atleticaRepository = atleticaRepository;
    this.dominioRepository = dominioRepository;
    this.cadastroService = cadastroService;
  }

  boolean jaInicializado() {
    return atleticaRepository.count() > 0;
  }

  Atleticas criar() {
    Atletica engenharia =
        cadastroService.criar(
            new AtleticaCreateRequest(
                ATLETICA_ENGENHARIA,
                SIGLA_ENGENHARIA,
                UNIVERSIDADE_EXEMPLO,
                null,
                FUSO_DEMONSTRACAO,
                true));
    Atletica saude =
        cadastroService.criar(
            new AtleticaCreateRequest(
                ATLETICA_SAUDE, SIGLA_SAUDE, UNIVERSIDADE_EXEMPLO, null, FUSO_DEMONSTRACAO, true));

    dominioRepository.save(
        new DominioEmailPermitido(engenharia, DominioEmail.normalize(ENGENHARIA_EXEMPLO_EDU_BR)));
    dominioRepository.save(
        new DominioEmailPermitido(saude, DominioEmail.normalize(SAUDE_EXEMPLO_EDU_BR)));
    return new Atleticas(engenharia, saude);
  }

  record Atleticas(Atletica engenharia, Atletica saude) {}
}
