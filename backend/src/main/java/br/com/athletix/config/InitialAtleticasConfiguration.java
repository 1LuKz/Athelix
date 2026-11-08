package br.com.athletix.config;

import br.com.athletix.atletica.dto.AtleticaCreateRequest;
import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.atletica.entity.DominioEmailPermitido;
import br.com.athletix.atletica.repository.AtleticaRepository;
import br.com.athletix.atletica.repository.DominioEmailPermitidoRepository;
import br.com.athletix.atletica.service.AtleticaCadastroService;
import br.com.athletix.atletica.service.DominioEmail;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

/** Dados de protótipo, restritos ao perfil de desenvolvimento. */
@Configuration
@Profile("dev")
public class InitialAtleticasConfiguration {

    @Bean
    CommandLineRunner initialAtleticas(
            AtleticaRepository atleticaRepository,
            DominioEmailPermitidoRepository dominioRepository,
            AtleticaCadastroService cadastroService
    ) {
        return args -> criarSeVazio(atleticaRepository, dominioRepository, cadastroService);
    }

    @Transactional
    void criarSeVazio(
            AtleticaRepository atleticaRepository,
            DominioEmailPermitidoRepository dominioRepository,
            AtleticaCadastroService cadastroService
    ) {
        if (atleticaRepository.count() > 0) {
            return;
        }

        Atletica engenharia = cadastroService.criar(new AtleticaCreateRequest(
                "Atlética de Engenharia Exemplo", "AAE", "Universidade Exemplo", null,
                "America/Sao_Paulo", true
        ));
        Atletica saude = cadastroService.criar(new AtleticaCreateRequest(
                "Atlética de Saúde Exemplo", "AAS", "Universidade Exemplo", null,
                "America/Sao_Paulo", true
        ));

        dominioRepository.save(new DominioEmailPermitido(engenharia, DominioEmail.normalize("engenharia.exemplo.edu.br")));
        dominioRepository.save(new DominioEmailPermitido(saude, DominioEmail.normalize("saude.exemplo.edu.br")));
    }
}
