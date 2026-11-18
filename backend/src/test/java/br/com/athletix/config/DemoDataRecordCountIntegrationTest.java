package br.com.athletix.config;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.athletix.atletica.repository.AtleticaRepository;
import br.com.athletix.atletica.repository.DominioEmailPermitidoRepository;
import br.com.athletix.cargo.repository.CargoRepository;
import br.com.athletix.departamento.repository.DepartamentoRepository;
import br.com.athletix.financeiro.repository.CategoriaFinanceiraRepository;
import br.com.athletix.gestao.repository.GestaoRepository;
import br.com.athletix.gestao.repository.VinculoGestaoRepository;
import br.com.athletix.membro.repository.MembroRepository;
import br.com.athletix.patrimonio.repository.CategoriaPatrimonioRepository;
import br.com.athletix.usuario.repository.UsuarioRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
class DemoDataRecordCountIntegrationTest {

  @Autowired private AtleticaRepository atleticas;
  @Autowired private DominioEmailPermitidoRepository dominios;
  @Autowired private CargoRepository cargos;
  @Autowired private DepartamentoRepository departamentos;
  @Autowired private UsuarioRepository usuarios;
  @Autowired private MembroRepository membros;
  @Autowired private GestaoRepository gestoes;
  @Autowired private VinculoGestaoRepository vinculos;
  @Autowired private CategoriaFinanceiraRepository categoriasFinanceiras;
  @Autowired private CategoriaPatrimonioRepository categoriasPatrimonio;

  @Test
  void mantemAsContagensDaCargaMinimaDeDemonstracao() {
    assertThat(contagensPorTabela())
        .containsExactlyInAnyOrderEntriesOf(
            Map.ofEntries(
                Map.entry("atleticas", 2L),
                Map.entry("dominios_email_permitidos", 2L),
                Map.entry("cargos", 2L),
                Map.entry("departamentos", 1L),
                Map.entry("usuarios", 2L),
                Map.entry("membros", 2L),
                Map.entry("gestoes", 1L),
                Map.entry("vinculos_gestao", 2L),
                Map.entry("categorias_financeiras", 6L),
                Map.entry("categorias_patrimonio", 4L)));
  }

  private Map<String, Long> contagensPorTabela() {
    Map<String, Long> contagens = new LinkedHashMap<>();
    contagens.put("atleticas", atleticas.count());
    contagens.put("dominios_email_permitidos", dominios.count());
    contagens.put("cargos", cargos.count());
    contagens.put("departamentos", departamentos.count());
    contagens.put("usuarios", usuarios.count());
    contagens.put("membros", membros.count());
    contagens.put("gestoes", gestoes.count());
    contagens.put("vinculos_gestao", vinculos.count());
    contagens.put("categorias_financeiras", categoriasFinanceiras.count());
    contagens.put("categorias_patrimonio", categoriasPatrimonio.count());
    return contagens;
  }
}
