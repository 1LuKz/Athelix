package br.com.athletix.config.demo;

import static br.com.athletix.config.demo.DemoData.CAMPO_NOME;
import static br.com.athletix.config.demo.DemoData.DEPARTAMENTO_INICIAL_DE_DESENVOLVIMENTO;
import static br.com.athletix.config.demo.DemoData.DEPARTAMENTO_ISOLADO_DE_DEMONSTRACAO;
import static br.com.athletix.config.demo.DemoData.DIRETORIA;
import static br.com.athletix.config.demo.DemoData.ESPORTES;
import static br.com.athletix.config.demo.DemoData.ORGANIZACAO_ESPORTIVA;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.departamento.entity.Departamento;
import br.com.athletix.departamento.repository.DepartamentoRepository;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.shared.normalization.NomeNormalizado;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
class DepartamentosDemoSeeder {
  private final DepartamentoRepository departamentoRepository;

  DepartamentosDemoSeeder(DepartamentoRepository departamentoRepository) {
    this.departamentoRepository = departamentoRepository;
  }

  Departamento criarDiretoria(Atletica engenharia) {
    Departamento diretoria =
        departamentoRepository.save(
            new Departamento(
                engenharia,
                DIRETORIA,
                NomeNormalizado.fromRequired(DIRETORIA, CAMPO_NOME),
                DEPARTAMENTO_INICIAL_DE_DESENVOLVIMENTO));
    return diretoria;
  }

  Departamento criarDiretoriaSaude(Atletica saude) {
    Departamento diretoriaSaude =
        departamentoRepository.save(
            new Departamento(
                saude,
                DIRETORIA,
                NomeNormalizado.fromRequired(DIRETORIA, CAMPO_NOME),
                DEPARTAMENTO_ISOLADO_DE_DEMONSTRACAO));
    return diretoriaSaude;
  }

  Departamento criarEsportes(Atletica engenharia) {
    Departamento esportes =
        departamentoRepository.save(
            new Departamento(
                engenharia,
                ESPORTES,
                NomeNormalizado.fromRequired(ESPORTES, CAMPO_NOME),
                ORGANIZACAO_ESPORTIVA));
    return esportes;
  }

  void ativar(Departamento departamento, Membro responsavel) {
    departamento.definirResponsavel(responsavel.getId());
    departamento.ativar();
    departamentoRepository.save(departamento);
  }
}
