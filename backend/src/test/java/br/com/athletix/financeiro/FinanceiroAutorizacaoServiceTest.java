package br.com.athletix.financeiro;

import br.com.athletix.cargo.entity.PerfilAcesso;
import br.com.athletix.financeiro.service.FinanceiroAutorizacaoService;
import br.com.athletix.membro.entity.Membro;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FinanceiroAutorizacaoServiceTest {
    private final FinanceiroAutorizacaoService service = new FinanceiroAutorizacaoService();

    @Test
    void permiteAdministradoresEFinanceiros() {
        assertDoesNotThrow(() -> service.exigirGerenciamentoFinanceiro(membro(PerfilAcesso.ADMINISTRADOR)));
        assertDoesNotThrow(() -> service.exigirGerenciamentoFinanceiro(membro(PerfilAcesso.FINANCEIRO)));
    }

    @Test
    void bloqueiaGestoresEMembros() {
        assertThrows(AccessDeniedException.class,
                () -> service.exigirGerenciamentoFinanceiro(membro(PerfilAcesso.GESTOR)));
        assertThrows(AccessDeniedException.class,
                () -> service.exigirGerenciamentoFinanceiro(membro(PerfilAcesso.MEMBRO)));
    }

    @Test
    void permiteConsultaAgregadaParaTodoAssociadoAtivo() {
        for (PerfilAcesso perfil : PerfilAcesso.values()) {
            assertDoesNotThrow(() -> service.exigirConsultaFinanceira(membro(perfil)));
        }
    }

    private Membro membro(PerfilAcesso perfil) {
        Membro membro = mock(Membro.class);
        when(membro.getPerfilAcesso()).thenReturn(perfil);
        return membro;
    }
}
