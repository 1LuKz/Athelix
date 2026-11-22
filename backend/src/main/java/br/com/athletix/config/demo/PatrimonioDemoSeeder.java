package br.com.athletix.config.demo;

import static br.com.athletix.config.demo.DemoData.*;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.patrimonio.dto.ItemPatrimonioCreateRequest;
import br.com.athletix.patrimonio.dto.ItemPatrimonioResponse;
import br.com.athletix.patrimonio.entity.CategoriaPatrimonio;
import br.com.athletix.patrimonio.repository.CategoriaPatrimonioRepository;
import br.com.athletix.patrimonio.service.ItemPatrimonioService;
import br.com.athletix.shared.normalization.NomeNormalizado;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
class PatrimonioDemoSeeder {
  private final CategoriaPatrimonioRepository categorias;
  private final ItemPatrimonioService itens;
  private final MovimentacoesPatrimonioDemoSeeder movimentacoes;

  PatrimonioDemoSeeder(
      CategoriaPatrimonioRepository categorias,
      ItemPatrimonioService itens,
      MovimentacoesPatrimonioDemoSeeder movimentacoes) {
    this.categorias = categorias;
    this.itens = itens;
    this.movimentacoes = movimentacoes;
  }

  void criar(CenarioDemo cenario) {
    List<ItemPatrimonioResponse> itensCriados = new ArrayList<>();
    for (DemoData.ItemDemo dados : ITENS_DEMO) {
      Long categoriaId =
          categorias.findAllByAtletica_IdOrderByNomeAsc(cenario.atletica().getId()).stream()
              .filter(categoria -> categoria.getNome().equals(dados.categoria()))
              .findFirst()
              .orElseThrow()
              .getId();
      itensCriados.add(
          itens.criar(
              new ItemPatrimonioCreateRequest(
                  dados.nome(),
                  LOTE_DEMONSTRATIVO,
                  categoriaId,
                  dados.quantidade(),
                  dados.conservacao(),
                  ALMOXARIFADO,
                  cenario.esportes().getId(),
                  dados.valor(),
                  true),
              cenario.administrador()));
    }
    movimentacoes.criar(cenario, itensCriados);
  }

  void criarCategorias(Atletica atletica) {
    criarCategoria(atletica, ELETRONICOS);
    criarCategoria(atletica, MATERIAL_ESPORTIVO);
  }

  private void criarCategoria(Atletica atletica, String nome) {
    categorias.save(
        new CategoriaPatrimonio(
            atletica, nome, NomeNormalizado.fromRequired(nome, CAMPO_NOME), true));
  }
}
