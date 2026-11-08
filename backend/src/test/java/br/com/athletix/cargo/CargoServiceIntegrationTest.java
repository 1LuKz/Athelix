package br.com.athletix.cargo;

import br.com.athletix.atletica.repository.AtleticaRepository;
import br.com.athletix.cargo.dto.CargoCreateRequest;
import br.com.athletix.cargo.entity.Cargo;
import br.com.athletix.cargo.entity.PerfilAcesso;
import br.com.athletix.cargo.service.CargoService;
import br.com.athletix.shared.exception.ConflictException;
import br.com.athletix.shared.tenancy.ContextoAtletica;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("dev")
class CargoServiceIntegrationTest {

    @Autowired
    private CargoService cargoService;

    @Autowired
    private AtleticaRepository atleticaRepository;

    @Autowired
    private ContextoAtletica contextoAtletica;

    @Test
    void requiresAdministratorAndEnforcesNormalizedNameUniqueness() {
        Long atleticaId = atleticaRepository.findAll().get(0).getId();
        CargoCreateRequest financeiro = new CargoCreateRequest(
                " Financeiro ", "Cuida das finanças", PerfilAcesso.FINANCEIRO, true
        );

        try (ContextoAtletica.Escopo ignored = contextoAtletica.abrir(atleticaId)) {
            assertThrows(AccessDeniedException.class, () -> cargoService.criar(financeiro, PerfilAcesso.GESTOR));

            Cargo cargo = cargoService.criar(financeiro, PerfilAcesso.ADMINISTRADOR);
            assertEquals("Financeiro", cargo.getNome());

            assertThrows(ConflictException.class, () -> cargoService.criar(
                    new CargoCreateRequest("financeiro", null, PerfilAcesso.FINANCEIRO, true),
                    PerfilAcesso.ADMINISTRADOR
            ));
        }
    }
}
