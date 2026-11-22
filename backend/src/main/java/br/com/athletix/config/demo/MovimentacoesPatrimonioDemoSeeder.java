package br.com.athletix.config.demo;

import static br.com.athletix.config.demo.DemoData.*;

import br.com.athletix.patrimonio.conferencia.dto.ConferenciaCreateRequest;
import br.com.athletix.patrimonio.conferencia.dto.ConferenciaDetalheResponse;
import br.com.athletix.patrimonio.conferencia.dto.ConferenciaResponse;
import br.com.athletix.patrimonio.conferencia.dto.ItemConferenciaResponse;
import br.com.athletix.patrimonio.conferencia.dto.ItemConferenciaUpdateRequest;
import br.com.athletix.patrimonio.conferencia.service.ConferenciaInventarioService;
import br.com.athletix.patrimonio.dto.ItemPatrimonioResponse;
import br.com.athletix.patrimonio.emprestimo.dto.DevolucaoPatrimonioCreateRequest;
import br.com.athletix.patrimonio.emprestimo.dto.EmprestimoPatrimonioCreateRequest;
import br.com.athletix.patrimonio.emprestimo.dto.EmprestimoPatrimonioResponse;
import br.com.athletix.patrimonio.emprestimo.service.EmprestimoPatrimonioService;
import br.com.athletix.patrimonio.entity.ConservacaoPatrimonio;
import br.com.athletix.patrimonio.manutencao.dto.ManutencaoPatrimonioCreateRequest;
import br.com.athletix.patrimonio.manutencao.service.ManutencaoPatrimonioService;
import java.time.Duration;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
class MovimentacoesPatrimonioDemoSeeder {
  private final EmprestimoPatrimonioService emprestimos;
  private final ManutencaoPatrimonioService manutencoes;
  private final ConferenciaInventarioService conferencias;

  MovimentacoesPatrimonioDemoSeeder(
      EmprestimoPatrimonioService emprestimos,
      ManutencaoPatrimonioService manutencoes,
      ConferenciaInventarioService conferencias) {
    this.emprestimos = emprestimos;
    this.manutencoes = manutencoes;
    this.conferencias = conferencias;
  }

  void criar(CenarioDemo cenario, List<ItemPatrimonioResponse> itens) {
    emprestimos.criar(
        new EmprestimoPatrimonioCreateRequest(
            itens.get(1).id(),
            cenario.associado().getId(),
            QUANTIDADE_EMPRESTADA,
            cenario.agora().plus(Duration.ofDays(FIM_EMPRESTIMO_DIAS)),
            EMPRESTIMO_DEMONSTRATIVO),
        cenario.administrador());
    EmprestimoPatrimonioResponse devolvido =
        emprestimos.criar(
            new EmprestimoPatrimonioCreateRequest(
                itens.get(0).id(),
                cenario.gestora().getId(),
                QUANTIDADE_EMPRESTADA,
                cenario.agora().plus(Duration.ofDays(FIM_EMPRESTIMO_DIAS)),
                EMPRESTIMO_DEMONSTRATIVO),
            cenario.administrador());
    emprestimos.devolver(
        devolvido.id(),
        new DevolucaoPatrimonioCreateRequest(
            QUANTIDADE_EMPRESTADA,
            null,
            ConservacaoPatrimonio.BOM,
            LOTE_DEMONSTRATIVO,
            false,
            null,
            null),
        cenario.administrador());
    manutencoes.criar(
        new ManutencaoPatrimonioCreateRequest(
            itens.get(2).id(),
            QUANTIDADE_EM_MANUTENCAO,
            REVISAO_DEMONSTRATIVA,
            cenario.agora().plus(Duration.ofDays(FIM_MANUTENCAO_DIAS))),
        cenario.administrador());
    criarConferenciaParcial(cenario);
  }

  private void criarConferenciaParcial(CenarioDemo cenario) {
    ConferenciaResponse conferencia =
        conferencias.criar(
            new ConferenciaCreateRequest(CONFERENCIA_PARCIAL), cenario.administrador());
    ConferenciaDetalheResponse detalhe =
        conferencias.detalhe(conferencia.id(), cenario.administrador());
    ItemConferenciaResponse primeiro = detalhe.itens().get(0);
    conferencias.conferir(
        conferencia.id(),
        primeiro.itemId(),
        new ItemConferenciaUpdateRequest(
            QUANTIDADE_CONFERIDA, DUAS_UNIDADES_EM_CONTAGEM, primeiro.version()),
        cenario.administrador());
    ItemConferenciaResponse segundo = detalhe.itens().get(1);
    conferencias.conferir(
        conferencia.id(),
        segundo.itemId(),
        new ItemConferenciaUpdateRequest(segundo.quantidadeEsperada(), null, segundo.version()),
        cenario.administrador());
  }
}
