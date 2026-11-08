package br.com.athletix.departamento;

import br.com.athletix.atletica.repository.AtleticaRepository;
import br.com.athletix.departamento.dto.DepartamentoCreateRequest;
import br.com.athletix.departamento.dto.DepartamentoTransitionRequest;
import br.com.athletix.departamento.entity.Departamento;
import br.com.athletix.departamento.entity.SituacaoDepartamento;
import br.com.athletix.departamento.service.DepartamentoService;
import br.com.athletix.cargo.entity.PerfilAcesso;
import br.com.athletix.shared.exception.BusinessRuleException;
import br.com.athletix.shared.tenancy.ContextoAtletica;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("dev")
class DepartamentoServiceIntegrationTest {

    @Autowired
    private DepartamentoService departamentoService;

    @Autowired
    private AtleticaRepository atleticaRepository;

    @Autowired
    private ContextoAtletica contextoAtletica;

    @Test
    void createsDepartmentInConfigurationOutsideTheActiveStructure() {
        Long atleticaId = atleticaRepository.findAll().stream()
                .filter(atletica -> "AAS".equals(atletica.getSigla()))
                .findFirst().orElseThrow().getId();

        try (ContextoAtletica.Escopo ignored = contextoAtletica.abrir(atleticaId)) {
            Departamento departamento = departamentoService.criarEmConfiguracao(
                    new DepartamentoCreateRequest("Eventos", "Organiza os eventos")
            );

            assertEquals(SituacaoDepartamento.EM_CONFIGURACAO, departamento.getSituacao());
            assertTrue(departamentoService.listarEstruturaAtivaDaAtleticaAtual().isEmpty());
            assertEquals(1, departamentoService.listarParaAdministracaoDaAtleticaAtual(PerfilAcesso.ADMINISTRADOR).size());
            assertThrows(AccessDeniedException.class,
                    () -> departamentoService.listarParaAdministracaoDaAtleticaAtual(PerfilAcesso.GESTOR));
            assertThrows(BusinessRuleException.class, () -> departamentoService.ativar(
                    departamento.getId(), new DepartamentoTransitionRequest(departamento.getVersion())
            ));
        }
    }
}
