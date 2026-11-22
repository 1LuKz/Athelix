package br.com.athletix.config.demo;

import static br.com.athletix.config.demo.DemoData.*;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.cargo.entity.Cargo;
import br.com.athletix.config.demo.AtleticasDemoSeeder.Atleticas;
import br.com.athletix.departamento.entity.Departamento;
import br.com.athletix.gestao.entity.Gestao;
import br.com.athletix.membro.entity.Membro;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@Profile("dev")
public class DemoDataInitializer {
  private final AtleticasDemoSeeder atleticas;
  private final DepartamentosDemoSeeder departamentos;
  private final CargosEMembrosDemoSeeder pessoas;
  private final GestoesDemoSeeder gestoes;
  private final FinanceiroDemoSeeder financeiro;
  private final PatrimonioDemoSeeder patrimonio;
  private final DemonstracaoCompletaSeeder demonstracaoCompleta;

  DemoDataInitializer(
      AtleticasDemoSeeder atleticas,
      DepartamentosDemoSeeder departamentos,
      CargosEMembrosDemoSeeder pessoas,
      GestoesDemoSeeder gestoes,
      FinanceiroDemoSeeder financeiro,
      PatrimonioDemoSeeder patrimonio,
      DemonstracaoCompletaSeeder demonstracaoCompleta) {
    this.atleticas = atleticas;
    this.departamentos = departamentos;
    this.pessoas = pessoas;
    this.gestoes = gestoes;
    this.financeiro = financeiro;
    this.patrimonio = patrimonio;
    this.demonstracaoCompleta = demonstracaoCompleta;
  }

  @Bean
  CommandLineRunner initialAtleticas(
      @Value("${athletix.demo.completa:false}") boolean demoCompleta) {
    return argumentos -> criarSeVazio(demoCompleta);
  }

  @Transactional
  void criarSeVazio(boolean demoCompleta) {
    if (atleticas.jaInicializado()) {
      return;
    }

    // Primeiro a estrutura mínima: atléticas, cargos, diretoria, contas, gestão e categorias.
    Atleticas base = atleticas.criar();
    EstruturaMinima estrutura = criarEstruturaMinima(base.engenharia());
    financeiro.criarCategorias(base.engenharia());
    financeiro.criarCategorias(base.saude());
    patrimonio.criarCategorias(base.engenharia());
    patrimonio.criarCategorias(base.saude());

    if (demoCompleta) {
      demonstracaoCompleta.criar(
          base, estrutura.administrador(), estrutura.tesoureiro(), estrutura.gestao());
    }
  }

  private EstruturaMinima criarEstruturaMinima(Atletica engenharia) {
    Cargo cargoAdministrador = pessoas.criarCargo(engenharia, PRESIDENCIA_DEMO);
    Cargo cargoFinanceiro = pessoas.criarCargo(engenharia, TESOURARIA_DEMO);
    Departamento diretoria = departamentos.criarDiretoria(engenharia);
    Membro administrador =
        pessoas.criarMembroDiretoria(
            engenharia, cargoAdministrador, diretoria, CONTA_ADMINISTRADOR, null);
    departamentos.ativar(diretoria, administrador);
    Gestao atual = gestoes.criarInicial(engenharia);
    gestoes.criarVinculo(administrador, atual, cargoAdministrador, diretoria);
    Membro tesoureiro =
        pessoas.criarMembroDiretoria(
            engenharia, cargoFinanceiro, diretoria, CONTA_FINANCEIRO, administrador);
    gestoes.criarVinculo(tesoureiro, atual, cargoFinanceiro, diretoria);
    return new EstruturaMinima(administrador, tesoureiro, atual);
  }

  private record EstruturaMinima(Membro administrador, Membro tesoureiro, Gestao gestao) {}
}
