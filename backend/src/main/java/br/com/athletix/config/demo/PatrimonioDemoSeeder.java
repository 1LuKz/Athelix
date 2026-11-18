package br.com.athletix.config.demo;

import static br.com.athletix.config.demo.DemoData.*;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.departamento.entity.Departamento;
import br.com.athletix.gestao.entity.Gestao;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.patrimonio.conferencia.entity.ConferenciaInventario;
import br.com.athletix.patrimonio.conferencia.entity.ItemConferencia;
import br.com.athletix.patrimonio.conferencia.repository.ConferenciaInventarioRepository;
import br.com.athletix.patrimonio.conferencia.repository.ItemConferenciaRepository;
import br.com.athletix.patrimonio.emprestimo.entity.EmprestimoPatrimonio;
import br.com.athletix.patrimonio.emprestimo.repository.EmprestimoPatrimonioRepository;
import br.com.athletix.patrimonio.entity.CategoriaPatrimonio;
import br.com.athletix.patrimonio.entity.ConservacaoPatrimonio;
import br.com.athletix.patrimonio.entity.ItemPatrimonio;
import br.com.athletix.patrimonio.manutencao.entity.ManutencaoPatrimonio;
import br.com.athletix.patrimonio.manutencao.repository.ManutencaoPatrimonioRepository;
import br.com.athletix.patrimonio.repository.CategoriaPatrimonioRepository;
import br.com.athletix.patrimonio.repository.ItemPatrimonioRepository;
import br.com.athletix.shared.normalization.NomeNormalizado;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
class PatrimonioDemoSeeder {
  private final CategoriaPatrimonioRepository categoriaPatrimonioRepository;
  private final ItemPatrimonioRepository itemPatrimonioRepository;
  private final EmprestimoPatrimonioRepository emprestimoRepository;
  private final ManutencaoPatrimonioRepository manutencaoRepository;
  private final ConferenciaInventarioRepository conferenciaRepository;
  private final ItemConferenciaRepository itemConferenciaRepository;

  PatrimonioDemoSeeder(
      CategoriaPatrimonioRepository categoriaPatrimonioRepository,
      ItemPatrimonioRepository itemPatrimonioRepository,
      EmprestimoPatrimonioRepository emprestimoRepository,
      ManutencaoPatrimonioRepository manutencaoRepository,
      ConferenciaInventarioRepository conferenciaRepository,
      ItemConferenciaRepository itemConferenciaRepository) {
    this.categoriaPatrimonioRepository = categoriaPatrimonioRepository;
    this.itemPatrimonioRepository = itemPatrimonioRepository;
    this.emprestimoRepository = emprestimoRepository;
    this.manutencaoRepository = manutencaoRepository;
    this.conferenciaRepository = conferenciaRepository;
    this.itemConferenciaRepository = itemConferenciaRepository;
  }

  void criar(
      Atletica engenharia,
      Gestao atual,
      Departamento esportes,
      Membro membroAtivo,
      Membro gestorMembro,
      Membro administradorMembro,
      Instant agora) {
    CategoriaPatrimonio eletronicos =
        categoriaPatrimonioRepository.findAllByAtletica_IdOrderByNomeAsc(engenharia.getId()).get(0);
    ItemPatrimonio disponivel =
        itemPatrimonioRepository.save(
            new ItemPatrimonio(
                engenharia,
                atual.getId(),
                BOLAS_OFICIAIS,
                LOTE_SINTETICO_DISPONIVEL,
                eletronicos,
                QUANTIDADE_BOLAS,
                ConservacaoPatrimonio.BOM,
                ALMOXARIFADO,
                esportes,
                new BigDecimal(VALOR_BOLA),
                true));
    ItemPatrimonio emprestado =
        itemPatrimonioRepository.save(
            new ItemPatrimonio(
                engenharia,
                atual.getId(),
                COLETES,
                LOTE_DEMONSTRATIVO,
                eletronicos,
                QUANTIDADE_COLETES,
                ConservacaoPatrimonio.OTIMO,
                ALMOXARIFADO,
                esportes,
                new BigDecimal(VALOR_COLETE),
                true));
    ItemPatrimonio manutencao =
        itemPatrimonioRepository.save(
            new ItemPatrimonio(
                engenharia,
                atual.getId(),
                CAIXA_DE_SOM,
                EQUIPAMENTO_SINTETICO,
                eletronicos,
                QUANTIDADE_CAIXAS_DE_SOM,
                ConservacaoPatrimonio.REGULAR,
                SALA_DE_EVENTOS,
                esportes,
                new BigDecimal(VALOR_300_REAIS),
                true));
    emprestimoRepository.save(
        new EmprestimoPatrimonio(
            engenharia,
            emprestado,
            membroAtivo.getId(),
            QUANTIDADE_EMPRESTADA,
            agora.minus(Duration.ofDays(INICIO_EMPRESTIMO_DIAS)),
            agora.plus(Duration.ofDays(FIM_EMPRESTIMO_DIAS)),
            EMPRESTIMO_DEMONSTRATIVO,
            gestorMembro.getId()));
    manutencaoRepository.save(
        new ManutencaoPatrimonio(
            engenharia,
            manutencao,
            QUANTIDADE_EM_MANUTENCAO,
            REVISAO_DEMONSTRATIVA,
            agora.minus(Duration.ofDays(INICIO_MANUTENCAO_DIAS)),
            agora.plus(Duration.ofDays(FIM_MANUTENCAO_DIAS)),
            gestorMembro.getId()));
    ConferenciaInventario conferencia =
        conferenciaRepository.save(
            new ConferenciaInventario(
                engenharia,
                atual.getId(),
                CONFERENCIA_PARCIAL,
                agora.minus(Duration.ofDays(INICIO_CONFERENCIA_DIAS)),
                administradorMembro.getId()));
    ItemConferencia linha =
        new ItemConferencia(conferencia, disponivel.getId(), disponivel.getQuantidadeTotal());
    linha.conferir(QUANTIDADE_CONFERIDA, DUAS_UNIDADES_EM_CONTAGEM, administradorMembro.getId());
    itemConferenciaRepository.save(linha);
  }

  void criarCategorias(Atletica atletica) {
    categoriaPatrimonioRepository.save(
        new CategoriaPatrimonio(
            atletica, ELETRONICOS, NomeNormalizado.fromRequired(ELETRONICOS, CAMPO_NOME), true));
    categoriaPatrimonioRepository.save(
        new CategoriaPatrimonio(
            atletica,
            MATERIAL_ESPORTIVO,
            NomeNormalizado.fromRequired(MATERIAL_ESPORTIVO, CAMPO_NOME),
            true));
  }
}
